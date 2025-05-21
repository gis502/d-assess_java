package com.ruoyi.system.service;

import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;

import java.util.List;

public interface IAssessmentOutputService  {

    // 产出图件
    public void outputMaps(AssessmentDTO assessmentDTO);
    // 产出
    public void outputReports(AssessmentDTO assessmentDTO);
    public List<AssessmentOutputDTO> getMap(String eqId,String eqqueueId);
}
