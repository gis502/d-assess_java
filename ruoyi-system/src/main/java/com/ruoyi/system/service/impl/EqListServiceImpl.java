package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.constant.BaseConstants;
import com.ruoyi.common.exception.EqReassessmentException;
import com.ruoyi.common.exception.EqTriggerException;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.utils.BaseUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.EqList;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.EqInfoDTO;
import com.ruoyi.system.domain.dto.ReassessmentDTO;
import com.ruoyi.system.domain.dto.TriggerDTO;
import com.ruoyi.system.domain.params.EqParams;
import com.ruoyi.system.mapper.EqListMapper;
import com.ruoyi.system.service.IAssessmentBatchService;
import com.ruoyi.system.service.IEqListService;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author: xiaodemos
 * @date: 2025-04-05 16:28
 * @description: 地震列表实现类
 */

@Slf4j
@Service
public class EqListServiceImpl implements IEqListService {

    @Resource
    private EqListMapper eqListMapper;
    @Resource
    private IAssessmentBatchService assessmentBatchService;

    // 启动地震
    // @Async("taskExecutor")
    @Override
    public EqParams trigger(TriggerDTO triggerDTO) {

        // 抛出异常
        if (triggerDTO == null) {
            throw new EqTriggerException(BaseConstants.TRIGGER_ERROR);
        }

        EqList eqList = new EqList();
        // 拷贝对象
        BeanUtils.copyProperties(triggerDTO, eqList);

        eqList.setId(UUID.randomUUID().toString());
        // 依据国标标准处理 eqId 地震类型 + 时间戳 + 编码
        String eqId = BaseUtils.generationCode(triggerDTO.getEqTime());
        String eqqueueId = eqId + "01";
        eqList.setEqId(eqId);
        eqList.setEqqueueId(eqqueueId);
        // 处理经纬度坐标
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(triggerDTO.getLongitude(), triggerDTO.getLatitude());
        Point point = geometryFactory.createPoint(coordinate);
        eqList.setGeom(point);
        eqList.setIntensity(0);

        // 处理地震名称 = 地震时间+地震名点+地震震级
        String fullName = triggerDTO.getEqTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + triggerDTO.getEqAddr()
                + triggerDTO.getMagnitude()
                + BaseConstants.FULL_NAME_SUFFIX;
        eqList.setEqFullName(fullName);
        eqList.setIsDeleted(0);
        eqList.setCreateTime(LocalDateTime.now());
        eqList.setUpdateTime(LocalDateTime.now());

        // 存入 eqList 表中
        int save = eqListMapper.insert(eqList);
        if (save > 0) {
            // 设置评估参数
            AssessmentDTO assessmentDTO = new AssessmentDTO();
            BeanUtils.copyProperties(triggerDTO, assessmentDTO);
            // 设置ID
            assessmentDTO.setEqId(eqId);
            assessmentDTO.setEqqueueId(eqqueueId);

            // 开始进行评估
            assessmentBatchService.assessment(assessmentDTO);
        }

        return new EqParams(eqId, eqqueueId);
    }

    // 重新评估
    @Async("taskExecutor")
    @Override
    public void reassessment(ReassessmentDTO reassessmentDTO) {

        // 抛出异常
        if (reassessmentDTO == null) {
            throw new EqReassessmentException(BaseConstants.REASSESSMENT_FILED);
        }

        // 条件构造
        LambdaQueryWrapper<EqList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EqList::getEqId, reassessmentDTO.getEqId());
        wrapper.eq(EqList::getIsDeleted, 0);

        EqList eqOne = eqListMapper.selectOne(wrapper);
        // 对比拷贝 (拷贝空对象？)
        BeanUtils.copyProperties(reassessmentDTO, eqOne);
        // 处理经纬度坐标
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(reassessmentDTO.getLongitude(), reassessmentDTO.getLatitude());
        Point point = geometryFactory.createPoint(coordinate);
        eqOne.setGeom(point);
        // 获取地震队列 id 并处理
        String eqqueueId = handlerEqqId(eqOne.getEqqueueId());
        eqOne.setEqqueueId(eqqueueId);

        log.info("地震重新评估参数{}", eqOne);

        // 更新到 eqList 表中
        int updated = eqListMapper.updateById(eqOne);
        if (updated > 0) {
            // 修改重新评估参数
            AssessmentDTO assessmentDTO = new AssessmentDTO();
            BeanUtils.copyProperties(reassessmentDTO, assessmentDTO);

            assessmentDTO.setEqqueueId(eqqueueId);
            // 开始进行评估
            assessmentBatchService.assessment(assessmentDTO);
        }
    }

    // 获取单场最新地震信息
    @Override
    public EqInfoDTO currently() {

        QueryWrapper wrapper = new QueryWrapper();
        // 设置查询条件
        wrapper.orderByDesc("update_time");
        wrapper.eq("is_deleted", 0);
        wrapper.last("limit 1");

        // 获取最新单场地震
        EqList currently = eqListMapper.selectOne(wrapper);
        if (currently == null) {
            throw new BaseException(BaseConstants.BASE_INFO_ERROR);
        }
        EqInfoDTO eqInfo = new EqInfoDTO();
        // 拷贝对象
        BeanUtils.copyProperties(currently, eqInfo);
        // 处理时间
        eqInfo.setEqTime(currently.getEqTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 处理经纬度转换
        eqInfo.setLongitude(currently.getGeom().getCoordinate().x);
        eqInfo.setLatitude(currently.getGeom().getCoordinate().y);

        return eqInfo;
    }

    // 处理 eqqId 批次
    private String handlerEqqId(String eqqId) {
        // 截取字符后 2 位
        String oldVersion = StringUtils.substring(eqqId, eqqId.length() - 2);
        // 将字符串转为整数，进行加法操作
        int num = Integer.parseInt(oldVersion.substring(1)) + 1;  // 只取后面的数字进行加法
        // 格式化为两位数（保留前导0）
        String version = String.format("%02d", num);
        log.info("{} 的批次已修改 {}", eqqId, version);

        return eqqId.substring(0, eqqId.length() - 2) + version;
    }

}
