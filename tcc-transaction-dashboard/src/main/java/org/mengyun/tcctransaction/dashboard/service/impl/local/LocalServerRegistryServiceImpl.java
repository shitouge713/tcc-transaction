package org.mengyun.tcctransaction.dashboard.service.impl.local;

import org.mengyun.tcctransaction.dashboard.service.condition.LocalStorageCondition;
import org.mengyun.tcctransaction.dashboard.service.impl.BaseServerRegistryServiceImpl;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

/**
 * @author Nervose.Wu
 * @date 2024/2/19 11:03
 */
@Conditional(LocalStorageCondition.class)
@Service
public class LocalServerRegistryServiceImpl extends BaseServerRegistryServiceImpl {
}
