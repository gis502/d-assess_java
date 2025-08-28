package com.ruoyi.system.service;

import com.ruoyi.system.domain.EqList;
import com.ruoyi.system.domain.dto.EqInfoDTO;
import com.ruoyi.system.domain.dto.ReassessmentDTO;
import com.ruoyi.system.domain.dto.TriggerDTO;
import com.ruoyi.system.domain.params.EqParams;

import java.util.concurrent.CompletableFuture;

public interface IEqListService {


    // 触发地震
    public EqParams trigger(TriggerDTO triggerDTO);

    public void reassessment(ReassessmentDTO reassessmentDTO);

    public EqInfoDTO currently();
}
