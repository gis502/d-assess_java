package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.exception.RainParamsException;
import com.ruoyi.common.exception.SeismicParamsException;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.core.drawers.RainLayoutDrawerService;
import com.ruoyi.system.core.drawers.SeismicLayoutDrawerService;
import com.ruoyi.system.domain.AssessmentOutput;
import com.ruoyi.system.domain.RainAssessmentOutput;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;
import com.ruoyi.system.domain.dto.RainAssessmentDTO;
import com.ruoyi.system.domain.dto.RainAssessmentOutputDTO;
import com.ruoyi.system.domain.params.EqParams;
import com.ruoyi.system.domain.params.RainParams;
import com.ruoyi.system.mapper.AssessmentOutputMapper;
import com.ruoyi.system.mapper.RainAssessmentOutputMapper;
import com.ruoyi.system.service.IAssessmentOutputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: xiaodemos
 * @date: 2025-04-06 12:49
 * @description: 图件产出服务类
 */

@Slf4j
@Service
public class AssessmentOutputServiceImpl implements IAssessmentOutputService {
    @Resource
    private AssessmentOutputMapper assessmentOutputMapper;
    @Resource
    private RainAssessmentOutputMapper rainAssessmentOutputMapper;
    @Resource
    private SeismicLayoutDrawerService seismicLayoutDrawerService;
    @Resource
    private RainLayoutDrawerService rainLayoutDrawerService;

    // 调用地震图件出图
    @Override
    public void outputMaps(AssessmentDTO assessmentDTO) {
        log.info("开始创建地震专题图...");
        // 只对 6.0 级以上的地震做专题图
        if (assessmentDTO.getMagnitude() >= BaseConstants.SEISMIC_6_GRADE) {
            // 调用图件产出服务进行出图
            seismicLayoutDrawerService.createSeismicPictureInit(assessmentDTO);
            log.info("专题图创建完成...");
        }
    }

    // 地震产出报告
    @Override
    public void outputReports(AssessmentDTO assessmentDTO) {
        log.info("开始生成灾情报告...");
        // 6级及以上地震生成 灾情报告 和 辅助决策报告
        if (assessmentDTO.getMagnitude() >= BaseConstants.SEISMIC_6_GRADE) {
            // TODO 灾情报告与辅助决策报告

        }
//        // 6级以下地震做辅助决策报告
//        if (assessmentDTO.getMagnitude() < BaseConstants.SEISMIC_6_GRADE)
//            reportPrepareService.prepareReport(assessmentDTO);

        log.info("灾情报告生成完成...");
    }

    // 获取地震专题图
    @Override
    public List<AssessmentOutputDTO> getMap(EqParams eqParams) {

        LambdaQueryWrapper<AssessmentOutput> wrapper = null;
        // 参数为空，抛出异常
        if (StringUtils.isEmpty(eqParams.getEqId()) && StringUtils.isEmpty(eqParams.getEqqueueId())) {
            throw new SeismicParamsException(BaseConstants.QUERY_PARAMS_ERROR);
        }

        // EqqueueId 只在查询批次时需要传入，否则不传入
        if (eqParams.getEqqueueId() == null) {
            // 条件构造器
            wrapper = Wrappers.lambdaQuery(AssessmentOutput.class)
                    .eq(AssessmentOutput::getEqId, eqParams.getEqId())
                    .eq(AssessmentOutput::getIsDeleted, 0);
        } else {
            wrapper = Wrappers.lambdaQuery(AssessmentOutput.class)
                    .eq(AssessmentOutput::getEqId, eqParams.getEqId())
                    .eq(AssessmentOutput::getEqqueueId, eqParams.getEqqueueId())
                    .eq(AssessmentOutput::getIsDeleted, 0);
        }
        // 获取对应Id的所有图件信息
        List<AssessmentOutput> mapOutput = assessmentOutputMapper.selectList(wrapper);
        // 专题图件为空 抛出异常
        if (mapOutput == null) {
            throw new BaseException(BaseConstants.THEMATIC_MAP_ERROR);
        }

        // 产出结果dto
        List<AssessmentOutputDTO> assessmentOutputDTOS = new ArrayList<>();
        for (AssessmentOutput output : mapOutput) {
            AssessmentOutputDTO outputDTO = AssessmentOutputDTO.builder().build();
            // 对象拷贝
            BeanUtils.copyProperties(output, outputDTO);
            // 加入结果集
            assessmentOutputDTOS.add(outputDTO);
        }
        return assessmentOutputDTOS;
    }

    // 暴雨产出图件
    @Override
    public void outputMaps(RainAssessmentDTO assessmentDTO) {
        log.info("开始创建暴雨专题图...");
        rainLayoutDrawerService.createSeismicPictureInit(assessmentDTO);
        log.info("专题图创建完成...");
    }
    // 暴雨报告
    @Override
    public void outputReports(RainAssessmentDTO assessmentDTO) {

    }

    // 获取地暴雨专题图
    @Override
    public List<RainAssessmentOutputDTO> getMap(RainParams rainParams) {

        LambdaQueryWrapper<RainAssessmentOutput> wrapper = null;
        // 参数为空，抛出异常
        if (StringUtils.isEmpty(rainParams.getRainId()) && StringUtils.isEmpty(rainParams.getRainQueueId())) {
            throw new RainParamsException(BaseConstants.QUERY_PARAMS_ERROR);
        }

        // RainQueueId 只在查询批次时需要传入，否则不传入
        if (rainParams.getRainQueueId() == null) {
            // 条件构造器
            wrapper = Wrappers.lambdaQuery(RainAssessmentOutput.class)
                    .eq(RainAssessmentOutput::getRainId, rainParams.getRainId())
                    .eq(RainAssessmentOutput::getIsDeleted, 0);
        } else {
            wrapper = Wrappers.lambdaQuery(RainAssessmentOutput.class)
                    .eq(RainAssessmentOutput::getRainId, rainParams.getRainId())
                    .eq(RainAssessmentOutput::getRainQueueId, rainParams.getRainQueueId())
                    .eq(RainAssessmentOutput::getIsDeleted, 0);
        }
        // 获取对应Id的所有图件信息
        List<RainAssessmentOutput> mapOutput = rainAssessmentOutputMapper.selectList(wrapper);
        // 专题图件为空 抛出异常
        if (mapOutput == null) {
            throw new BaseException(BaseConstants.THEMATIC_MAP_ERROR);
        }

        // 产出结果dto
        List<RainAssessmentOutputDTO> assessmentOutputDTOS = new ArrayList<>();
        for (RainAssessmentOutput output : mapOutput) {
            RainAssessmentOutputDTO outputDTO = RainAssessmentOutputDTO.builder().build();
            // 对象拷贝
            BeanUtils.copyProperties(output, outputDTO);
            // 加入结果集
            assessmentOutputDTOS.add(outputDTO);
        }
        return assessmentOutputDTOS;


    }

}
