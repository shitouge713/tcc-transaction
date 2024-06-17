package org.mengyun.tcctransaction.dashboard.service.impl;

import org.mengyun.tcctransaction.dashboard.dto.RegistryStatusDto;
import org.mengyun.tcctransaction.dashboard.dto.ResponseDto;
import org.mengyun.tcctransaction.dashboard.enums.ResponseCodeEnum;
import org.mengyun.tcctransaction.dashboard.exception.TransactionException;
import org.mengyun.tcctransaction.dashboard.service.ServerRegistryService;

import java.util.Collections;
import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2024/2/19 10:59
 */
public class BaseServerRegistryServiceImpl implements ServerRegistryService {
    @Override
    public ResponseDto<List<RegistryStatusDto>> queryStatus() {
        return ResponseDto.returnSuccess(Collections.emptyList());
    }

    @Override
    public ResponseDto<Void> online(String ip, Integer port) {
        throw new TransactionException(ResponseCodeEnum.NOT_SUPPORT);
    }

    @Override
    public ResponseDto<Void> offline(String ip, Integer port) {
        throw new TransactionException(ResponseCodeEnum.NOT_SUPPORT);
    }
}
