package org.mengyun.tcctransaction.dashboard.dto;

/**
 * @author Nervose.Wu
 * @date 2024/2/19 11:23
 */
public class AddressDto {
    private String ip;
    private Integer port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
