package org.mengyun.tcctransaction.discovery.registry.zookeeper;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.mengyun.tcctransaction.discovery.registry.AbstractRegistryService;
import org.mengyun.tcctransaction.discovery.registry.RegistryConfig;
import org.mengyun.tcctransaction.discovery.registry.RegistryStatus;
import org.mengyun.tcctransaction.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 16:33
 */
public class ZookeeperRegistryServiceImpl extends AbstractRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryServiceImpl.class.getSimpleName());

    private static final String BASE_PATH = "/tcc/server/";

    private static final String BASE_PATH_FOR_DASHBOARD = "/tcc/server-for-dashboard/";

    private ZookeeperRegistryProperties properties;

    private String parentPath;

    private String parentPathForDashboard;

    private ZookeeperInstance instance;

    private ZookeeperInstance backupInstance;

    public ZookeeperRegistryServiceImpl(RegistryConfig registryConfig) {
        setClusterName(registryConfig.getClusterName());
        this.properties = registryConfig.getZookeeperRegistryProperties();
        this.parentPath = BASE_PATH + getClusterName();
        this.parentPathForDashboard = BASE_PATH_FOR_DASHBOARD + getClusterName();
        this.instance = new ZookeeperInstance(properties.getConnectString(), properties.getDigest(), buildCurator(false));
        if (!StringUtils.isEmpty(properties.getBackupConnectString()) && !Objects.equals(properties.getConnectString(), properties.getBackupConnectString())) {
            this.backupInstance = new ZookeeperInstance(properties.getBackupConnectString(), properties.getBackupDigest(), buildCurator(true));
        }
    }

    private CuratorFramework buildCurator(boolean isBackup) {
        String connectString = isBackup ? properties.getBackupConnectString() : properties.getConnectString();
        String digest = isBackup ? properties.getBackupDigest() : properties.getDigest();

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(properties.getSessionTimeout())
                .connectionTimeoutMs(properties.getConnectTimeout())
                .retryPolicy(new ExponentialBackoffRetry(properties.getBaseSleepTime(), properties.getMaxRetries()));

        if (StringUtils.isNotEmpty(digest)) {
            builder.authorization("digest", digest.getBytes())
                    .aclProvider(new ACLProvider() {
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        return builder.build();
    }

    @Override
    public void start() {
        start(instance);
        if (backupInstance != null) {
            start(backupInstance);
        }
    }

    @Override
    public RegistryStatus queryServerRegistryStatus() {
        RegistryStatus status = instance.queryServerRegistryStatus();
        if (backupInstance != null) {
            status = RegistryStatus.combine(status, backupInstance.queryServerRegistryStatus());
        }
        return status;
    }

    @Override
    public void serverOnline() {
        instance.serverOnline();
        if (backupInstance != null) {
            backupInstance.serverOnline();
        }
    }

    @Override
    public void serverOffline() {
        instance.serverOffline();
        if (backupInstance != null) {
            backupInstance.serverOffline();
        }
    }

    private void start(ZookeeperInstance target) {
        target.start(properties.getConnectTimeout());
    }

    @Override
    protected void doRegister(InetSocketAddress address, InetSocketAddress addressForDashboard) throws InterruptedException {
        instance.register(parentPath, address, parentPathForDashboard, addressForDashboard);
        if (backupInstance != null) {
            backupInstance.register(parentPath, address, parentPathForDashboard, addressForDashboard);
        }
    }

    @Override
    protected void doSubscribe(boolean addressForDashboard) {
        doSubscribe(instance, addressForDashboard);
        if (backupInstance != null) {
            doSubscribe(backupInstance, addressForDashboard);
        }
    }

    private void doSubscribe(ZookeeperInstance target, boolean addressForDashboard) {
        try {
            createParentNode(target.getCurator(), addressForDashboard);
        } catch (Exception ignore) {
            /*
             * In most cases, the parent node will be created by the server in advance, so we can ignore this exception.
             * However, if the parent node does not exist, the subscription will become invalid even if the zookeeper is restored.
             */
        }

        String path = addressForDashboard ? parentPathForDashboard : parentPath;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(target.getCurator(), path, false);
        pathChildrenCache.getListenable().addListener((curator, pathChildrenCacheEvent) -> {
            switch (pathChildrenCacheEvent.getType()) {
                case CHILD_ADDED:
                case CHILD_REMOVED:
                case CHILD_UPDATED:
                    try {
                        updateServiceList(target.getCurator(), addressForDashboard);
                    } catch (Exception e) {
                        logger.warn("Failed to update server addresses", e);
                    }
                    break;
                default:
                    break;
            }
        });
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RegistryException("Failed to subscribe", e);
        }
        try {
            updateServiceList(target.getCurator(), addressForDashboard);
        } catch (Exception e) {
            //ignore
        }
    }

    @Override
    public void close() {
        instance.close();
        if (backupInstance != null) {
            backupInstance.close();
        }
    }

    private void updateServiceList(CuratorFramework curator, boolean addressForDashboard) throws Exception {
        String path = addressForDashboard ? parentPathForDashboard : parentPath;
        List<String> nodePaths = curator.getChildren().forPath(path);
        List<String> newServerAddresses = new ArrayList<>();
        for (String nodePath : nodePaths) {
            newServerAddresses.add(new String(curator.getData().forPath(path + "/" + nodePath), StandardCharsets.UTF_8));
        }
        setServerAddresses(newServerAddresses, addressForDashboard);
    }

    private void createParentNode(CuratorFramework target, boolean addressForDashboard) throws Exception {
        String path = addressForDashboard ? parentPathForDashboard : parentPath;
        if (target.checkExists().forPath(path) == null) {
            try {
                target.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path, "".getBytes());
            } catch (KeeperException.NodeExistsException ignore) {
            }
        }
    }
}
