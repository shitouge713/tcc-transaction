package org.mengyun.tcctransaction.server.controller;

import org.mengyun.tcctransaction.TccServer;
import org.mengyun.tcctransaction.dashboard.dto.ResponseDto;
import org.mengyun.tcctransaction.discovery.registry.RegistryService;
import org.mengyun.tcctransaction.discovery.registry.RegistryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nervose.Wu
 * @date 2024/2/18 16:30
 */
@RestController
@RequestMapping("registry")
public class RegistryController {

    static final Logger logger = LoggerFactory.getLogger(RegistryController.class.getSimpleName());

    @Autowired
    private TccServer tccServer;

    @PostMapping("/online")
    public ResponseDto<Void> online(){
        for (RegistryService registryService : tccServer.getRegistryServices()) {
            try {
                registryService.serverOnline();
            }catch (Exception e){
                logger.warn("failed to online server",e);
            }
        }
        return ResponseDto.returnSuccess();
    }

    @PostMapping("/offline")
    public ResponseDto<Void> offline() {
        for (RegistryService registryService : tccServer.getRegistryServices()) {
            try {
                registryService.serverOffline();
            } catch (Exception e) {
                logger.warn("failed to offline server", e);
            }
        }
        return ResponseDto.returnSuccess();
    }

    @GetMapping("/queryStatus")
    public ResponseDto<Integer> queryStatus() {
        RegistryStatus status = null;
        for (RegistryService registryService : tccServer.getRegistryServices()) {
            if (status == null) {
                status = registryService.queryServerRegistryStatus();
            } else {
                status = RegistryStatus.combine(status, registryService.queryServerRegistryStatus());
            }
        }
        return ResponseDto.returnSuccess(status == null ? RegistryStatus.UNKNOWN.getId() : status.getId());
    }
}
