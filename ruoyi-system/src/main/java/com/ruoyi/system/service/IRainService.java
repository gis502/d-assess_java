package com.ruoyi.system.service;

import com.ruoyi.system.domain.dto.RainReassessmentDTO;
import com.ruoyi.system.domain.dto.RainTriggerDTO;
import com.ruoyi.system.domain.dto.ReassessmentDTO;
import com.ruoyi.system.domain.dto.TriggerDTO;
import com.ruoyi.system.domain.params.RainParams;

public interface IRainService {


    // 触发地震
    public RainParams trigger(RainTriggerDTO triggerDTO);

    public void reassessment(RainReassessmentDTO reassessmentDTO);


}
