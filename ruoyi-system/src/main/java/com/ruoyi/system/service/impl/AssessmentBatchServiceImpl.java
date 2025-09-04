package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.exception.AssessmentException;
import com.ruoyi.system.domain.AssessmentBatch;
import com.ruoyi.system.domain.EarthQuakeReportEntity;
import com.ruoyi.system.domain.Hospital;
import com.ruoyi.system.domain.RainAssessmentBatch;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.TriggerDTO;
import com.ruoyi.system.domain.dto.RainAssessmentDTO;
import com.ruoyi.system.mapper.AssessmentBatchMapper;
import com.ruoyi.system.mapper.HospitalMapper;
import com.ruoyi.system.mapper.RainAssessmentBatchMapper;
import com.ruoyi.system.service.IAssessmentBatchService;
import com.ruoyi.system.service.IAssessmentOutputService;
import com.ruoyi.system.service.IEarthQuakeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author: xiaodemos
 * @date: 2025-04-04 15:23
 * @description: 批次评估业务类
 */

@Slf4j
@Service
public class AssessmentBatchServiceImpl implements IAssessmentBatchService {

    @Resource
    private AssessmentBatchMapper assessmentBatchMapper;
    @Resource
    private RainAssessmentBatchMapper rainAssessmentBatchMapper;
    @Resource
    private IAssessmentOutputService assessmentOutputService;
    //测试完删除
//    @Resource
//    private HospitalMapper hospitalMapper;
//    @Resource
//    private IEarthQuakeService earthQuakeService;

    // 设置评估结束标志
    private Boolean isOutput = true, isReport = true, isAffected = true, isSeismic = true;

    /**
     * @param assessmentDTO 评估参数
     * @author: xiaodemos
     * @date: 2025/4/5 22:35
     * @description: 进行多个类型的数据评估
     * @return: 返回评估完成状态
     */
    @Async("taskExecutor")
    @Override
    public void assessment(AssessmentDTO assessmentDTO) {
        log.info("地震数据开始评估...", assessmentDTO);

        AssessmentBatch assessmentBatch = new AssessmentBatch();
        // 设置参数
        assessmentBatch.setEqId(assessmentDTO.getEqId());
        assessmentBatch.setEqqueueId(assessmentDTO.getEqqueueId());
        assessmentBatch.setBatch(BaseConstants.ASSESSMENT_INIT);
        assessmentBatch.setType(BaseConstants.ARTI_TRIGGER);
        assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_NOTHING);
        assessmentBatch.setProgress(BaseConstants.PROGRESS_ZERO);
        assessmentBatch.setIsDeleted(0);    // 逻辑删除
        assessmentBatch.setId(UUID.randomUUID().toString());    // 生成uuid
        assessmentBatch.setCreateTime(LocalDateTime.now());     // 创建时间
        assessmentBatch.setUpdateTime(LocalDateTime.now());     // 修改时间

        // 设置条件构造器
        LambdaQueryWrapper<AssessmentBatch> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AssessmentBatch::getEqId, assessmentDTO.getEqId());

        // 插入到批次表中
        int insert = assessmentBatchMapper.insert(assessmentBatch);
        if (insert > 0) {
            try {
                // 设置评估状态为正在计算中
                assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_COMPUTING);
                assessmentBatchMapper.updateById(assessmentBatch);
                // 进行图片产出
                assessmentOutputService.outputMaps(assessmentDTO);
                // TODO 进行报告产出

                //测试用
//                EarthQuakeReportEntity reportEntity = new EarthQuakeReportEntity();
//                reportEntity = getEarthquakeEntity(assessmentDTO);
//                earthQuakeService.generateEarthQuakeReport(reportEntity);
//                if (input){
                assessmentOutputService.outputReports(assessmentDTO);
//                }

                // TODO 进行经济损失评估

                // TODO 进行地震影响场评估

            } catch (AssessmentException e) {
                // 如果出现错误则抛出异常 并设置异常结束状态
                assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_ABNORMAL);
                assessmentBatchMapper.updateById(assessmentBatch);
                e.printStackTrace();
                // TODO 需要做补偿机制、调用记录在 消息队列 中的失败记录进行重新评估
                reassessmentTry();
                // TODO 需要做重新的判断，是否所有的评估已经完成

            }
            // 所有出图结束 将评估状态修改为已完成
            if (isOutput && isReport && isAffected && isSeismic) {
                assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_FINISH);
                assessmentBatchMapper.updateById(assessmentBatch);
            }
        }
    }


    // 暴雨评估
    @Async("taskExecutor")
    @Override
    public void assessment(RainAssessmentDTO assessmentDTO) {

        log.info("暴雨数据开始评估...", assessmentDTO);

        RainAssessmentBatch assessmentBatch = new RainAssessmentBatch();
        // 设置参数
        assessmentBatch.setRainId(assessmentDTO.getRainId());
        assessmentBatch.setRainQueueId(assessmentDTO.getRainQueueId());
        assessmentBatch.setBatch(BaseConstants.ASSESSMENT_INIT);
        assessmentBatch.setType(BaseConstants.ARTI_TRIGGER);
        assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_NOTHING);
        assessmentBatch.setProgress(BaseConstants.PROGRESS_ZERO);
        assessmentBatch.setIsDeleted(0);    // 逻辑删除
        assessmentBatch.setId(UUID.randomUUID().toString());    // 生成uuid
        assessmentBatch.setCreateTime(LocalDateTime.now());     // 创建时间
        assessmentBatch.setUpdateTime(LocalDateTime.now());     // 修改时间

        // 设置条件构造器
        LambdaQueryWrapper<RainAssessmentBatch> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RainAssessmentBatch::getRainId, assessmentDTO.getRainId());

        // 插入到批次表中
        int insert = rainAssessmentBatchMapper.insert(assessmentBatch);
        if (insert > 0) {
            try {
                // 设置评估状态为正在计算中
                assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_COMPUTING);
                rainAssessmentBatchMapper.updateById(assessmentBatch);
                // 进行图片产出
                assessmentOutputService.outputMaps(assessmentDTO);
                // TODO 进行报告产出

                // TODO 进行经济损失评估

                // TODO 进行地震影响场评估



            } catch (AssessmentException e) {
                // 如果出现错误则抛出异常 并设置异常结束状态
                assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_ABNORMAL);
                rainAssessmentBatchMapper.updateById(assessmentBatch);
                e.printStackTrace();
                // TODO 需要做补偿机制、调用记录在 消息队列 中的失败记录进行重新评估
                reassessmentTry();
                // TODO 需要做重新的判断，是否所有的评估已经完成

            }

            // 所有出图结束 将评估状态修改为已完成
            if (isOutput && isReport) {
                assessmentBatch.setState(BaseConstants.ASSESSMENT_STATE_FINISH);
                rainAssessmentBatchMapper.updateById(assessmentBatch);
            }
        }


    }

    // TODO 只做一次重新评估
    private void reassessmentTry() {

    }


    //测试完删除
//    private EarthQuakeReportEntity getEarthquakeEntity(AssessmentDTO assessmentDTO) {
//        EarthQuakeReportEntity reportEntity = new EarthQuakeReportEntity();
//        reportEntity.setEqId(assessmentDTO.getEqId());
//        reportEntity.setEqqueueId(assessmentDTO.getEqqueueId());
//        /*
//         *地震概况部分
//         */
//        reportEntity.setReportTime(assessmentDTO.getEqTime()); // 报告时间
//        reportEntity.setEarthQuakeTime(assessmentDTO.getEqTime()); // 地震时间
//        reportEntity.setEarthQuakePosition(assessmentDTO.getEqAddr()); // 地震位置
//        reportEntity.setEarthQuakeLon(assessmentDTO.getLongitude()); //震源经度
//        reportEntity.setEarthQuakeLat(assessmentDTO.getLatitude()); //震源纬度
//        reportEntity.setEarthQuakeMagnitude(assessmentDTO.getMagnitude());//震级
//        reportEntity.setEarthQuakeSourceDepth(assessmentDTO.getEqDepth());//震源深度
//        /*
//         * 风险评估部分
//         */
//        reportEntity.setEarthQuakeIntensity(assessmentDTO.getIntensity());//重灾区烈度
//        reportEntity.setEarthQuakeDisasterArea(assessmentDTO.getCircleArea());//重灾区面积(km2)
//        reportEntity.setEarthQuakeInfluencePopulationMax(assessmentDTO.getAffectPopMax());//地震影响人口最大值
//        reportEntity.setEarthQuakeInfluencePopulationMin(assessmentDTO.getAffectPopMin());//地震影响人口最小值
//        reportEntity.setEarthQuakeDeathMax(assessmentDTO.getDiePopMax());//地震预计伤亡人数最大值
//        reportEntity.setEarthQuakeDeathMin(assessmentDTO.getDiePopMin());//地震预计伤亡人数最小值
//        reportEntity.setEarthQuakeFaultZone(assessmentDTO.getFaultZone());//震中最近断裂带
//
//        List<com.ruoyi.system.domain.Hospital> dbHospitals = hospitalMapper.selectHospitAffectPoints(
//                assessmentDTO.getLongitude(),
//                assessmentDTO.getLatitude()
//        );
//        List<EarthQuakeReportEntity.Hospital> reportHospitals = new ArrayList<>();
//        for (com.ruoyi.system.domain.Hospital dbHospital : dbHospitals) {
//            // 创建报告内部类的Hospital对象（注意：必须通过外部类实例创建，因为是非静态内部类）
//            EarthQuakeReportEntity.Hospital reportHospital = reportEntity.new Hospital();
//
//            // 赋值：数据库实体属性 -> 报告内部类属性（字段对应关系需根据实际需求调整）
//            reportHospital.setHospitalName(dbHospital.getName()); // 医院名称
//            reportHospital.setHospitalBeds(dbHospital.getBeds() != null ? dbHospital.getBeds().toString() : "0"); // 总床位（转String，匹配内部类字段类型）
//            reportHospital.setHospitalAddress(dbHospital.getAddress()); // 医院地址
//            reportHospital.setHospitalLevel(dbHospital.getLevel()); // 医院等级（如三级甲等）
//
//            // 将转换后的对象加入报告列表
//            reportHospitals.add(reportHospital);
//        }
//        reportEntity.setEarthQuakeHospital(reportHospitals);
//        if (reportEntity.getEarthQuakeMagnitude()>= 7.0){
//            reportEntity.setEarthQuakeEmergencyLevel("一级");
//        }else if (reportEntity.getEarthQuakeMagnitude()>= 6.0){
//            reportEntity.setEarthQuakeEmergencyLevel("二级");
//        }else if (reportEntity.getEarthQuakeMagnitude()>= 5.0){
//            reportEntity.setEarthQuakeEmergencyLevel("三级");
//        }else {
//            reportEntity.setEarthQuakeEmergencyLevel("四级");
//        }
//        return reportEntity;
//
//    }

}
