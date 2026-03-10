package com.ruoyi.system.listener;

import com.rabbitmq.client.Channel;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.exception.ThematicReceiveException;
import com.ruoyi.common.utils.QiniuOssUtil;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.domain.AssessmentOutput;
import com.ruoyi.system.domain.RainAssessmentOutput;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;
import com.ruoyi.system.domain.dto.RainAssessmentOutputDTO;
import com.ruoyi.system.mapper.AssessmentOutputMapper;
import com.ruoyi.system.mapper.RainAssessmentOutputMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author: xiaodemos
 * @date: 2025-04-12 16:47
 * @description: 获取专题图监听器
 */

@Slf4j
@Component
public class ThematicReceiverListener {

    //    @Resource
//    private QiniuOssUtil qiniuOssUtil;
    @Resource
    private AssessmentOutputMapper assessmentOutputMapper;
    @Resource
    private RainAssessmentOutputMapper rainAssessmentOutputMapper;

    // rabbitmq 监听专题图队列
    @RabbitListener(queues = "thematic.map", containerFactory = "rabbitListenerContainerFactory")
    public void receive(AssessmentOutputDTO outputDTO, Message message, Channel channel) {
        // 打印日志
        log.info("rabbitmq 接收到 {} ...", outputDTO.getFileName());

        try {
            // 设置图件产出信息
            AssessmentOutput assessmentOutput = new AssessmentOutput();
            assessmentOutput.setIsDeleted(0);    // 逻辑删除
            assessmentOutput.setId(UUID.randomUUID().toString());    // 生成 uuid
            assessmentOutput.setCreateTime(LocalDateTime.now());     // 创建时间
            assessmentOutput.setUpdateTime(LocalDateTime.now());     // 修改时间

            // 数据拷贝
            BeanUtils.copyProperties(outputDTO, assessmentOutput);
            // 确保存储路径正确（使用 sourceFile 而不是 localSourceFile）
            if (StringUtils.isNotEmpty(outputDTO.getSourceFile())) {
                assessmentOutput.setSourceFile(outputDTO.getSourceFile());
            } else {
                // 如果 sourceFile 为空，使用 localSourceFile 作为备用
                assessmentOutput.setSourceFile(outputDTO.getLocalSourceFile());
            }
            // 将图件信息插入到结果表中
            log.info("开始存库...{}, 存储路径：{}", assessmentOutput, assessmentOutput.getSourceFile());
            assessmentOutputMapper.insert(assessmentOutput);
            log.info("{} 成功保存到数据库...", assessmentOutput.getFileName());
        } catch (Exception ex) {
            log.info("存库失败...{}", ex.getMessage());

            // 抛出异常
            throw new ThematicReceiveException(BaseConstants.THEMATIC_MAP_ERROR);
        }
    }

    @RabbitListener(queues = "rain.map", containerFactory = "rabbitListenerContainerFactory")
    public void receive(RainAssessmentOutputDTO outputDTO, Message message, Channel channel) {
        // 打印日志
        log.info("rabbitmq 接收到 {} ...", outputDTO.getFileName());
        try {
            // 设置图件产出信息
            RainAssessmentOutput assessmentOutput = new RainAssessmentOutput();
            assessmentOutput.setIsDeleted(0);    // 逻辑删除
            assessmentOutput.setId(UUID.randomUUID().toString());    // 生成 uuid
            assessmentOutput.setCreateTime(LocalDateTime.now());     // 创建时间
            assessmentOutput.setUpdateTime(LocalDateTime.now());     // 修改时间

            // 数据拷贝+存库逻辑
            BeanUtils.copyProperties(outputDTO, assessmentOutput);
            // 确保存储路径正确（使用 sourceFile 而不是 localSourceFile）
            if (StringUtils.isNotEmpty(outputDTO.getSourceFile())) {
                assessmentOutput.setSourceFile(outputDTO.getSourceFile());
            } else {
                // 如果 sourceFile 为空，使用 localSourceFile 作为备用
                assessmentOutput.setSourceFile(outputDTO.getLocalSourceFile());
            }
            log.info("开始存库...{}, 存储路径：{}", assessmentOutput, assessmentOutput.getSourceFile());
            rainAssessmentOutputMapper.insert(assessmentOutput);
            log.info("{} 成功保存到数据库...", outputDTO.getFileName());

        } catch (Exception ex) {
            log.error("存库/处理失败...{}", ex.getMessage(), ex);
            throw new ThematicReceiveException(BaseConstants.THEMATIC_MAP_ERROR);
        }
    }
}
