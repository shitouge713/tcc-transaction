package org.mengyun.tcctransaction.discovery.registry;

import org.mengyun.tcctransaction.discovery.registry.direct.DirectRegistryProperties;
import org.mengyun.tcctransaction.discovery.registry.nacos.NacosRegistryProperties;
import org.mengyun.tcctransaction.discovery.registry.zookeeper.ZookeeperRegistryProperties;

/**
 *
 * 注册配置操作接口类
 * @author Nervose.Wu
 * @date 2022/5/12 18:03
 */
public interface RegistryConfig {

    String getClusterName();

    ZookeeperRegistryProperties getZookeeperRegistryProperties();

    NacosRegistryProperties getNacosRegistryProperties();

    DirectRegistryProperties getDirectRegistryProperties();

    String getCustomRegistryName();
}
