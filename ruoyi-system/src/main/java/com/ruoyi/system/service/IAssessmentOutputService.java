package com.ruoyi.system.service;

import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;
import com.ruoyi.system.domain.dto.RainAssessmentDTO;
import com.ruoyi.system.domain.dto.RainAssessmentOutputDTO;
import com.ruoyi.system.domain.params.EqParams;
import com.ruoyi.system.domain.params.RainParams;

import java.util.List;

public interface IAssessmentOutputService  {

    // 地震部分

    // 产出图件
    public void outputMaps(AssessmentDTO assessmentDTO);
    // 产出报告
    public void outputReports(AssessmentDTO assessmentDTO);
    // 获取图件
    public List<AssessmentOutputDTO> getMap(EqParams eqParams);


    // 暴雨部分

    // 产出图件
    public void outputMaps(RainAssessmentDTO assessmentDTO);
    // 产出报告
    public void outputReports(RainAssessmentDTO assessmentDTO);
    // 获取图件
    public List<RainAssessmentOutputDTO> getMap(RainParams rainParams);




}
