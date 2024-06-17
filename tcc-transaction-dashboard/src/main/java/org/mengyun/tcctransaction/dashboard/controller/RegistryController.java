package org.mengyun.tcctransaction.dashboard.controller;

import org.mengyun.tcctransaction.dashboard.dto.AddressDto;
import org.mengyun.tcctransaction.dashboard.dto.RegistryStatusDto;
import org.mengyun.tcctransaction.dashboard.dto.ResponseDto;
import org.mengyun.tcctransaction.dashboard.service.ServerRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Nervose.Wu
 * @date 2024/2/19 10:28
 */
@RestController
@RequestMapping("/api/registry")
public class RegistryController {

    @Autowired
    private ServerRegistryService serverRegistryService;

    @GetMapping("/queryStatus")
    public ResponseDto<List<RegistryStatusDto>> queryStatus() {
        return serverRegistryService.queryStatus();
    }

    @PostMapping("/online")
    public ResponseDto<Void> online(@RequestBody AddressDto addressDto) {
        return serverRegistryService.online(addressDto.getIp(), addressDto.getPort());
    }

    @PostMapping("/offline")
    public ResponseDto<Void> offline(@RequestBody AddressDto addressDto) {
        return serverRegistryService.offline(addressDto.getIp(), addressDto.getPort());
    }
}
