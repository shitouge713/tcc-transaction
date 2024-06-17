package org.mengyun.tcctransaction.discovery.registry.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.mengyun.tcctransaction.discovery.registry.RegistryStatus;
import org.mengyun.tcctransaction.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Nervose.Wu
 * @date 2023/2/7 14:31
 */
public class ZookeeperInstance {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperInstance.class.getSimpleName());

    private String connectString;
    private String digest;
    private final CuratorFramework curator;

    private boolean connected = false;

    private String nodePath;

    private byte[] nodeData;

    private String nodePathForDashboard;

    private byte[] nodeDataForDashboard;

    private final AtomicReference<PersistentNode> nodeRefer = new AtomicReference<>();

    private final AtomicReference<PersistentNode> nodeReferForDashboard = new AtomicReference<>();

    public ZookeeperInstance(String connectString, String digest, CuratorFramework curator) {
        this.connectString = connectString;
        this.digest = digest;
        this.curator = curator;
    }

    public void start(int maxWaitTime) {
        curator.getConnectionStateListenable().addListener((client, connectionState) -> {
            connected = connectionState.isConnected();
        });
        curator.start();
        boolean connected = false;
        try {
            connected = curator.blockUntilConnected(maxWaitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //ignore
        }
        if (!connected) {
            logger.error("Cant connect to the zookeeper {}. The server may not be discovered until zookeeper is restored!", connectString);
        }
    }
    public void register(String parentPath, InetSocketAddress address, String parentPathForDashboard, InetSocketAddress addressForDashboard) throws InterruptedException {
        this.nodePath = parentPath + "/node-" + NetUtils.parseSocketAddress(address).replaceAll("/", "");
        this.nodeData = NetUtils.parseSocketAddress(address).getBytes(StandardCharsets.UTF_8);
        this.nodePathForDashboard = parentPathForDashboard + "/node-" + NetUtils.parseSocketAddress(addressForDashboard).replaceAll("/", "");
        this.nodeDataForDashboard = NetUtils.parseSocketAddress(addressForDashboard).getBytes(StandardCharsets.UTF_8);
        PersistentNode node = new PersistentNode(curator, CreateMode.EPHEMERAL, false, nodePath, nodeData);
        if (nodeRefer.compareAndSet(null, node)) {
            node.start();
            node.waitForInitialCreate(1000, TimeUnit.MILLISECONDS);
        }

        PersistentNode nodeForDashboard = new PersistentNode(curator, CreateMode.EPHEMERAL, false, nodePathForDashboard, nodeDataForDashboard);
        if (nodeReferForDashboard.compareAndSet(null, nodeForDashboard)) {
            nodeForDashboard.start();
            nodeForDashboard.waitForInitialCreate(1000, TimeUnit.MILLISECONDS);
        }
    }

    public void serverOnline() {
        PersistentNode node = new PersistentNode(curator, CreateMode.EPHEMERAL, false, nodePath, nodeData);
        if (nodeRefer.compareAndSet(null, node)) {
            node.start();
        }
    }

    public void serverOffline() {
        PersistentNode node = nodeRefer.get();
        if (node != null && nodeRefer.compareAndSet(node, null)) {
            CloseableUtils.closeQuietly(node);
        }
    }

    public RegistryStatus queryServerRegistryStatus() {
        if (!connected) {
            return RegistryStatus.UNKNOWN;
        }
        try {
            if (curator.checkExists().forPath(nodePath) != null) {
                return RegistryStatus.ONLINE;
            } else {
                return RegistryStatus.OFFLINE;
            }
        } catch (Exception e) {
            logger.warn("failed to query server status", e);
            return RegistryStatus.UNKNOWN;
        }
    }

    public void close() {
        try {
            PersistentNode node = nodeRefer.get();
            if (node != null && nodeRefer.compareAndSet(node, null)) {
                CloseableUtils.closeQuietly(node);
            }
            PersistentNode nodeForDashboard = nodeReferForDashboard.get();
            if (nodeForDashboard != null && nodeReferForDashboard.compareAndSet(nodeForDashboard, null)) {
                CloseableUtils.closeQuietly(node);
            }
        } catch (Exception e) {
            //ignore
        }
        try {
            curator.close();
        } catch (Exception e) {
            //ignore
        }
    }

    public CuratorFramework getCurator() {
        return curator;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}
