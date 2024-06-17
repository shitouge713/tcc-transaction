package org.mengyun.tcctransaction.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储类型枚举
 */
public enum StorageType {

    MEMORY("MEMORY"),

    JDBC("JDBC"),

    REDIS_CLUSTER("REDIS_CLUSTER"),

    REDIS("REDIS"),

    SHARD_REDIS("SHARD_REDIS"),

    ROCKSDB("ROCKSDB"),

    REMOTING("REMOTING"),

    CUSTOMIZED("CUSTOMIZED");

    private static Map<StorageType, String> storageClassNameMap = new HashMap<>();

    /**
     * 为何还要多此一举呢，直接通过枚获取不就可以么，为啥还要通过map
     */
    static {
        storageClassNameMap.put(MEMORY, MemoryTransactionStorage.class.getName());
        storageClassNameMap.put(JDBC, JdbcTransactionStorage.class.getName());
        storageClassNameMap.put(REDIS_CLUSTER, JedisClusterTransactionStorage.class.getName());
        storageClassNameMap.put(REDIS, RedisTransactionStorage.class.getName());
        storageClassNameMap.put(SHARD_REDIS, ShardJedisTransactionStorage.class.getName());
        storageClassNameMap.put(ROCKSDB, RocksDbTransactionStorage.class.getName());
        storageClassNameMap.put(REMOTING, RemotingTransactionStorage.class.getName());
    }

    private String value;

    StorageType(String value) {
        this.value = value;
    }

    public static String getStorageClassName(StorageType storageType) {
        return storageClassNameMap.get(storageType);
    }

    public String value() {
        return this.value;
    }

}
