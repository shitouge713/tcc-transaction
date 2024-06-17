package org.mengyun.tcctransaction.dashboard.dto;

/**
 * @author Nervose.Wu
 * @date 2024/2/19 10:49
 */
public class RegistryStatusDto {
    private String ip;
    private Integer port;
    private Integer status;

    public RegistryStatusDto(String ip, Integer port, Integer status) {
        this.ip = ip;
        this.port = port;
        this.status = status;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
