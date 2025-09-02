package com.ruoyi.system.service;

import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.RainAssessmentDTO;

public interface IAssessmentBatchService {

    // 地震评估
    public void assessment(AssessmentDTO assessmentDTO);
    // 暴雨评估
    public void assessment(RainAssessmentDTO assessmentDTO);
}
