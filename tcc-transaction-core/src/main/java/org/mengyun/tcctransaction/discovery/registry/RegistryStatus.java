package org.mengyun.tcctransaction.discovery.registry;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nervose.Wu
 * @date 2024/2/6 10:53
 */
public enum RegistryStatus {
    ONLINE(1,"已上线"),
    OFFLINE(2,"已下线"),
    UNKNOWN(3,"未知"),
    ;

    private int id;
    private String desc;

    RegistryStatus(int id, String desc) {
        this.id=id;
        this.desc = desc;
    }

    private static Map<Integer, RegistryStatus> idValuePairs;

    static {
        idValuePairs = Arrays.stream(values()).collect(Collectors.toMap(RegistryStatus::getId, Function.identity()));
    }

    public static RegistryStatus valueOf(int id) {
        return idValuePairs.get(id);
    }

    public static RegistryStatus combine(RegistryStatus ... registryStatuses){
        RegistryStatus firstStatus=registryStatuses[0];
        for(int i=1;i<registryStatuses.length;i++){
            if(registryStatuses[i]!=firstStatus){
                return RegistryStatus.UNKNOWN;
            }
        }
        return firstStatus;
    }

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }
}
