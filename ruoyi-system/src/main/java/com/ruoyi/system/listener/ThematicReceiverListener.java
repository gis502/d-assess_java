package com.ruoyi.system.listener;

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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
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

    @Resource
    private QiniuOssUtil qiniuOssUtil;
    @Resource
    private AssessmentOutputMapper assessmentOutputMapper;
    @Resource
    private RainAssessmentOutputMapper rainAssessmentOutputMapper;

    // rabbitmq 监听专题图队列
    @RabbitListener(queues = "thematic.map")
    public void receive(AssessmentOutputDTO outputDTO) {
        // 打印日志
        log.info("rabbitmq 接收到 {} ...", outputDTO.getFileName());

        try {
            // 设置图件产出信息
            AssessmentOutput assessmentOutput = new AssessmentOutput();
            assessmentOutput.setIsDeleted(0);    // 逻辑删除
            assessmentOutput.setId(UUID.randomUUID().toString());    // 生成uuid
            assessmentOutput.setCreateTime(LocalDateTime.now());     // 创建时间
            assessmentOutput.setUpdateTime(LocalDateTime.now());     // 修改时间

            File originFile = new File(outputDTO.getLocalSourceFile());

            if (!originFile.exists()) {
                throw new ThematicReceiveException(BaseConstants.FILE_NOT_FOUND_ERROR);
            }

            // 获取对应图片文件二进制流
            MultipartFile file = new MockMultipartFile(originFile.getName(), originFile.getName(), "image/jpeg", new FileInputStream(originFile));
            // 将图片设置唯一Id
            String imageUrl = outputDTO.getEqqueueId() + "_" + outputDTO.getFileName();

            // 将图片上传到七牛云服务器
            String qiniuUrl = qiniuOssUtil.upload(imageUrl, file);
            // 上传失败
            if (StringUtils.isEmpty(qiniuUrl)) {
                throw new ThematicReceiveException(BaseConstants.THEMATIC_MAP_ERROR);
            }
            log.info("{} 成功上传到七牛云服务器...", outputDTO.getFileName());
            // 数据拷贝
            BeanUtils.copyProperties(outputDTO, assessmentOutput);
            // 修改存储路径
            assessmentOutput.setSourceFile(qiniuUrl);
            // 将图件信息插入到结果表中
            assessmentOutputMapper.insert(assessmentOutput);
            log.info("{} 成功保存到数据库...", assessmentOutput.getFileName());

        } catch (Exception ex) {
            ex.printStackTrace();
            // 抛出异常
            throw new ThematicReceiveException(BaseConstants.THEMATIC_MAP_ERROR);
        }
    }
    @RabbitListener(queues = "rain.map")
    public void receive(RainAssessmentOutputDTO outputDTO) {
        // 打印日志
        log.info("rabbitmq 接收到 {} ...", outputDTO.getFileName());
        try {
            // 设置图件产出信息
            RainAssessmentOutput assessmentOutput = new RainAssessmentOutput();
            assessmentOutput.setIsDeleted(0);    // 逻辑删除
            assessmentOutput.setId(UUID.randomUUID().toString());    // 生成uuid
            assessmentOutput.setCreateTime(LocalDateTime.now());     // 创建时间
            assessmentOutput.setUpdateTime(LocalDateTime.now());     // 修改时间

            File originFile = new File(outputDTO.getLocalSourceFile());

            if (!originFile.exists()) {
                throw new ThematicReceiveException(BaseConstants.FILE_NOT_FOUND_ERROR);
            }

            // 获取对应图片文件二进制流
            MultipartFile file = new MockMultipartFile(originFile.getName(), originFile.getName(), "image/jpeg", new FileInputStream(originFile));
            // 将图片设置唯一Id
            String imageUrl = outputDTO.getRainQueueId() + "_" + outputDTO.getFileName();

            // 将图片上传到七牛云服务器
            String qiniuUrl = qiniuOssUtil.upload(imageUrl, file);
            // 上传失败
            if (StringUtils.isEmpty(qiniuUrl)) {
                throw new ThematicReceiveException(BaseConstants.THEMATIC_MAP_ERROR);
            }
            log.info("{} 成功上传到七牛云服务器...", outputDTO.getFileName());
            // 数据拷贝
            BeanUtils.copyProperties(outputDTO, assessmentOutput);
            // 修改存储路径
            assessmentOutput.setSourceFile(qiniuUrl);
            // 将图件信息插入到结果表中
            rainAssessmentOutputMapper.insert(assessmentOutput);
            log.info("{} 成功保存到数据库...", assessmentOutput.getFileName());

        } catch (Exception ex) {
            ex.printStackTrace();
            // 抛出异常
            throw new ThematicReceiveException(BaseConstants.THEMATIC_MAP_ERROR);
        }
    }
}
