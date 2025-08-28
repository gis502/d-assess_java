package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.exception.AssessmentException;
import com.ruoyi.system.domain.AssessmentBatch;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.mapper.AssessmentBatchMapper;
import com.ruoyi.system.service.IAssessmentBatchService;
import com.ruoyi.system.service.IAssessmentOutputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private IAssessmentOutputService assessmentOutputService;

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

    // TODO 只做一次重新评估
    private void reassessmentTry() {

    }

}
