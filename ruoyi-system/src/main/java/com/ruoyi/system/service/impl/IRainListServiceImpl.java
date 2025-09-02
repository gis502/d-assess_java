package com.ruoyi.system.service.impl;

import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.exception.EqTriggerException;
import com.ruoyi.common.utils.BaseUtils;
import com.ruoyi.system.domain.RainList;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.RainAssessmentDTO;
import com.ruoyi.system.domain.dto.RainReassessmentDTO;
import com.ruoyi.system.domain.dto.RainTriggerDTO;
import com.ruoyi.system.domain.params.EqParams;
import com.ruoyi.system.domain.params.RainParams;
import com.ruoyi.system.mapper.RainListMapper;
import com.ruoyi.system.service.IAssessmentBatchService;
import com.ruoyi.system.service.IRainService;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: xiaodemos
 * @date: 2025-08-30 15:42
 * @description: 暴雨产图业务层
 */

@Slf4j
@Service
public class IRainListServiceImpl implements IRainService {


    @Resource
    private RainListMapper rainListMapper;
    @Resource
    private IAssessmentBatchService assessmentBatchService;

    // 启动暴雨
    @Override
    public RainParams trigger(RainTriggerDTO triggerDTO) {

        // 抛出异常
        if (triggerDTO == null) {
            throw new EqTriggerException(BaseConstants.TRIGGER_ERROR);
        }

        RainList rainList = new RainList();
        // 拷贝对象
        BeanUtils.copyProperties(triggerDTO, rainList);

        // 依据国标标准处理 eqId 地震类型 + 时间戳 + 编码
        String rainId = BaseUtils.generationRainCode(triggerDTO.getOccurrenceTime());
        String rainQueueId = rainId + "01";
        rainList.setRainId(rainId);
        rainList.setRainQueueId(rainQueueId);
        // 处理经纬度坐标
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(triggerDTO.getLongitude(), triggerDTO.getLatitude());
        Point point = geometryFactory.createPoint(coordinate);
        rainList.setGeom(point);

        // 处理暴雨名称 = 暴雨时间+暴雨地点+持续时间+降雨量
        String fullName = triggerDTO.getOccurrenceTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + triggerDTO.getPosition()
                + triggerDTO.getDuration()
                + BaseConstants.RAIN_NAME_PREFIX
                + triggerDTO.getRainfall()
                + BaseConstants.RAIN_NAME_SUFFIX;
        rainList.setDisasterName(fullName);
        rainList.setIsDeleted(0);
        rainList.setCreateTime(LocalDateTime.now());
        rainList.setUpdateTime(LocalDateTime.now());

        // 存入 eqList 表中
        int save = rainListMapper.insert(rainList);
        if (save > 0) {
            // 设置评估参数
            RainAssessmentDTO assessmentDTO = new RainAssessmentDTO();
            BeanUtils.copyProperties(triggerDTO, assessmentDTO);
            // 设置ID
            assessmentDTO.setRainId(rainId);
            assessmentDTO.setRainQueueId(rainQueueId);
            // 开始进行评估
            assessmentBatchService.assessment(assessmentDTO);
        }

        return new RainParams(rainId, rainQueueId);
    }





    @Override
    public void reassessment(RainReassessmentDTO reassessmentDTO) {

    }




}
