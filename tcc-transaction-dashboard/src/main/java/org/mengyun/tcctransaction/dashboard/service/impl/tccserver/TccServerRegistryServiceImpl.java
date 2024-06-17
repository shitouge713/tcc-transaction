package org.mengyun.tcctransaction.dashboard.service.impl.tccserver;

import com.netflix.loadbalancer.Server;
import org.mengyun.tcctransaction.dashboard.constants.DashboardConstant;
import org.mengyun.tcctransaction.dashboard.dto.RegistryStatusDto;
import org.mengyun.tcctransaction.dashboard.dto.ResponseDto;
import org.mengyun.tcctransaction.dashboard.enums.ResponseCodeEnum;
import org.mengyun.tcctransaction.dashboard.service.ServerRegistryService;
import org.mengyun.tcctransaction.dashboard.service.condition.TccServerStorageCondition;
import org.mengyun.tcctransaction.discovery.registry.RegistryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2024/2/19 11:05
 */
@Conditional(TccServerStorageCondition.class)
@Service
public class TccServerRegistryServiceImpl implements ServerRegistryService {

    @Value("${feign.path}")
    private String serverContextPath;

    private static final String REQUEST_METHOD_REGISTRY_OFFLINE = "registry/offline";
    private static final String REQUEST_METHOD_REGISTRY_ONLINE = "registry/online";
    private static final String REQUEST_METHOD_QUERY_REGISTRY_STATUS = "registry/queryStatus";

    private static final Logger logger = LoggerFactory.getLogger(TccServerRegistryServiceImpl.class);

    @Autowired
    private SpringClientFactory springClientFactory;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseDto<List<RegistryStatusDto>> queryStatus() {
        List<RegistryStatusDto> statusDtos = new ArrayList<>();
        List<Server> servers = springClientFactory.getLoadBalancer(DashboardConstant.TCC_SERVER_GROUP).getReachableServers();
        for (Server server : servers) {
            String requestUrl = "http://"
                    .concat(server.getHostPort())
                    .concat("/")
                    .concat(serverContextPath)
                    .concat("/")
                    .concat(REQUEST_METHOD_QUERY_REGISTRY_STATUS);
            try {
                ResponseDto<Integer> responseDto = restTemplate.getForObject(requestUrl, ResponseDto.class);
                if (responseDto.isSuccess()) {
                    statusDtos.add(new RegistryStatusDto(server.getHost(), server.getPort(), responseDto.getData()));
                } else {
                    statusDtos.add(new RegistryStatusDto(server.getHost(), server.getPort(), RegistryStatus.UNKNOWN.getId()));
                }
            } catch (Exception e) {
                statusDtos.add(new RegistryStatusDto(server.getHost(), server.getPort(), RegistryStatus.UNKNOWN.getId()));
                logger.warn("query registry status failed, requestUrl:{}", requestUrl, e);
            }
        }
        return ResponseDto.returnSuccess(statusDtos);
    }

    @Override
    public ResponseDto<Void> online(String ip, Integer port) {
        String requestUrl = "http://"
                .concat(ip + ":" + port)
                .concat("/")
                .concat(serverContextPath)
                .concat("/")
                .concat(REQUEST_METHOD_REGISTRY_ONLINE);
        try {
            logger.info("online server {}:{}", ip, port);
            return restTemplate.postForObject(requestUrl, null, ResponseDto.class);
        } catch (Exception e) {
            logger.warn("server online failed, requestUrl:{}", requestUrl, e);
            return ResponseDto.returnFail(ResponseCodeEnum.SERVER_ONLINE_ERROR);
        }
    }

    @Override
    public ResponseDto<Void> offline(String ip, Integer port) {
        String requestUrl = "http://"
                .concat(ip + ":" + port)
                .concat("/")
                .concat(serverContextPath)
                .concat("/")
                .concat(REQUEST_METHOD_REGISTRY_OFFLINE);
        try {
            logger.info("offline server {}:{}", ip, port);
            return restTemplate.postForObject(requestUrl, null, ResponseDto.class);
        } catch (Exception e) {
            logger.warn("server offline failed, requestUrl:{}", requestUrl, e);
            return ResponseDto.returnFail(ResponseCodeEnum.SERVER_OFFLINE_ERROR);
        }
    }
}
