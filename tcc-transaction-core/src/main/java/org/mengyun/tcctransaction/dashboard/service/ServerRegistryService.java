package org.mengyun.tcctransaction.dashboard.service;

import org.mengyun.tcctransaction.dashboard.dto.RegistryStatusDto;
import org.mengyun.tcctransaction.dashboard.dto.ResponseDto;

import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2024/2/19 10:56
 */
public interface ServerRegistryService {
    ResponseDto<List<RegistryStatusDto>> queryStatus();

    ResponseDto<Void> online(String ip, Integer port);

    ResponseDto<Void> offline(String ip, Integer port);
}
