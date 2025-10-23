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
import com.ruoyi.system.domain.EarthQuakeReportEntity;
import com.ruoyi.system.domain.Hospital;
import com.ruoyi.system.domain.RainAssessmentOutput;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.domain.dto.AssessmentOutputDTO;
import com.ruoyi.system.domain.dto.RainAssessmentDTO;
import com.ruoyi.system.domain.dto.RainAssessmentOutputDTO;
import com.ruoyi.system.domain.params.EqParams;
import com.ruoyi.system.domain.params.RainParams;
import com.ruoyi.system.entity.FireFighter;
import com.ruoyi.system.entity.StorePoints;
import com.ruoyi.system.mapper.*;
import com.ruoyi.system.service.IAssessmentOutputService;
import com.ruoyi.system.service.IEarthQuakeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @Resource
    private HospitalMapper hospitalMapper;
    @Resource
    private IEarthQuakeService earthQuakeService;
    @Resource
    private FireFighterMapper fireFighterMapper;
    @Resource
    private StorePointsMapper storePointsMapper;


    // 调用地震图件出图
    @Override
    public void outputMaps(AssessmentDTO assessmentDTO) {
        // 只对 6.0 级以上的地震做专题图
        if (assessmentDTO.getMagnitude() >= BaseConstants.SEISMIC_6_GRADE) {
            log.info("开始创建地震专题图...");
            // 调用图件产出服务进行出图
            seismicLayoutDrawerService.createSeismicPictureInit(assessmentDTO);
            log.info("专题图创建完成...");
        }
    }

    // 地震产出报告
    @Override
    public void outputReports(AssessmentDTO assessmentDTO) {
        log.info("开始生成灾情报告...");
        // 常量定义：提取魔法值为常量，提高可维护性
        final int REQUIRED_IMAGE_COUNT = 15;
        final long MAX_WAIT_TIME = 300000; // 最大等待时间5分钟（毫秒）
        final long CHECK_INTERVAL = 5000; // 检查间隔5秒（毫秒）

        try {
            if (assessmentDTO.getMagnitude() >= BaseConstants.SEISMIC_6_GRADE) {
                // 6级及以上地震：生成灾情报告和辅助决策报告
                EarthQuakeReportEntity reportEntity = getEarthquakeEntity(assessmentDTO);
                EqParams eqParams = new EqParams();
                eqParams.setEqId(assessmentDTO.getEqId());
                eqParams.setEqqueueId(assessmentDTO.getEqqueueId());
                log.info("查询参数：{}", eqParams);

                // 获取并等待足够的图片（最多等待5分钟）
                List<AssessmentOutputDTO> earthquakeGraphs = waitForEnoughImages(eqParams, REQUIRED_IMAGE_COUNT, MAX_WAIT_TIME, CHECK_INTERVAL);

                // 处理图片数据到报告实体
                mapImagesToReport(earthquakeGraphs, reportEntity);

                // 生成报告
                earthQuakeService.generateEarthQuakeReport(reportEntity);
                log.info("6级及以上地震报告生成完毕");
            } else {
                // 6级以下地震：仅生成辅助决策报告（修复原代码注释问题）
//                reportPrepareService.prepareReport(assessmentDTO);
                log.info("6级以下地震辅助决策报告生成完毕");
            }

            log.info("灾情报告生成流程完成");
        } catch (Exception e) {
            log.error("生成灾情报告失败", e);
            throw new RuntimeException("报告生成异常", e); // 包装异常，保留堆栈信息
        }
    }

    /**
     * 等待获取足够数量的图片
     * @param eqParams 查询参数
     * @param requiredCount 所需图片数量
     * @param maxWaitTime 最大等待时间(毫秒)
     * @param checkInterval 检查间隔(毫秒)
     * @return 符合数量要求的图片列表
     * @throws InterruptedException 线程中断异常
     */
    private List<AssessmentOutputDTO> waitForEnoughImages(EqParams eqParams, int requiredCount,
                                                          long maxWaitTime, long checkInterval) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        List<AssessmentOutputDTO> images;

        do {
            images = getMap(eqParams);
            if (images.size() >= requiredCount) {
                log.info("已获取足够图片，数量：{}", images.size());
                return images;
            }

            log.info("当前图片数量不足({}/{}), 等待{}ms后重试",
                    images.size(), requiredCount, checkInterval);
            Thread.sleep(checkInterval);
        } while (System.currentTimeMillis() - startTime < maxWaitTime);

        // 超时处理：可根据业务需求调整（抛异常或继续执行）
        log.warn("超过最大等待时间{}ms，图片数量仍不足，继续执行", maxWaitTime);
        return images;
    }

    /**
     * 将图片映射到报告实体的对应字段
     * @param images 图片列表
     * @param reportEntity 报告实体
     */
    private void mapImagesToReport(List<AssessmentOutputDTO> images, EarthQuakeReportEntity reportEntity) {
        for (AssessmentOutputDTO image : images) {
            // 使用Java 8兼容的传统switch语句替代增强switch表达式
            String fileName = image.getFileName();
            switch (fileName) {
                case "震区附近医院分布图":
                    reportEntity.setEarthQuakeHospitalGraph(image.getSourceFile());
                    break;
                case "影响估计范围分布图":
                    reportEntity.setEarthQuakeInfluenceGraph(image.getSourceFile());
                    break;
                case "震区附近断层分布图":
                    reportEntity.setEarthQuakeFaultZoneGraph(image.getSourceFile());
                    break;
                case "震区附近救援队伍分布图":
                    reportEntity.setEarthQuakeFireFighterGraph(image.getSourceFile());
                    break;
                case "震区附近救援物资分布图":
                    reportEntity.setEarthQuakeStorePointGraph(image.getSourceFile());
                    break;
                // 可根据需要添加更多图片类型的映射
                default:
                    log.debug("未处理的图片类型：{}", fileName);
            }
        }
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
        // 累计降雨量高于 30mm 触发专题图
        if (Float.parseFloat(assessmentDTO.getRainfall()) > 30) {
            log.info("开始创建暴雨专题图...");
            rainLayoutDrawerService.createSeismicPictureInit(assessmentDTO);
            log.info("专题图创建完成...");
        }
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

    //计算报告所需要的参数
    private EarthQuakeReportEntity getEarthquakeEntity(AssessmentDTO assessmentDTO) {
        EarthQuakeReportEntity reportEntity = new EarthQuakeReportEntity();
        reportEntity.setEqId(assessmentDTO.getEqId());
        reportEntity.setEqqueueId(assessmentDTO.getEqqueueId());
        /*
         *地震概况部分
         */
        reportEntity.setReportTime(assessmentDTO.getEqTime()); // 报告时间
        reportEntity.setEarthQuakeTime(assessmentDTO.getEqTime()); // 地震时间
        reportEntity.setEarthQuakePosition(assessmentDTO.getEqAddr()); // 地震位置
        reportEntity.setEarthQuakeLon(assessmentDTO.getLongitude()); //震源经度
        reportEntity.setEarthQuakeLat(assessmentDTO.getLatitude()); //震源纬度
        reportEntity.setEarthQuakeMagnitude(assessmentDTO.getMagnitude());//震级
        reportEntity.setEarthQuakeSourceDepth(assessmentDTO.getEqDepth());//震源深度
        /*
         * 风险评估部分
         */
        reportEntity.setEarthQuakeIntensity(assessmentDTO.getIntensity());//重灾区烈度
        reportEntity.setEarthQuakeDisasterArea(assessmentDTO.getCircleArea());//重灾区面积(km2)
        reportEntity.setEarthQuakeInfluencePopulationMax(assessmentDTO.getAffectPopMax());//地震影响人口最大值
        reportEntity.setEarthQuakeInfluencePopulationMin(assessmentDTO.getAffectPopMin());//地震影响人口最小值
        reportEntity.setEarthQuakeDeathMax(assessmentDTO.getDiePopMax());//地震预计伤亡人数最大值
        reportEntity.setEarthQuakeDeathMin(assessmentDTO.getDiePopMin());//地震预计伤亡人数最小值
        reportEntity.setEarthQuakeFaultZone(assessmentDTO.getFaultZone());//震中最近断裂带
        //计算高级烈度区的长短轴
        double magnitude = assessmentDTO.getMagnitude();
        int intensity = Integer.parseInt(assessmentDTO.getIntensity());
        double semiMajorAxis1 = (Math.exp((3.04+1.27*magnitude-intensity)/0.92)-8.65)*150;
        double semiMinorAxis1 = (Math.exp((2.57+1.23*magnitude-intensity)/0.86)-4.86)*150;
        double semiMajorAxis2 = (Math.exp((4.04+1.27*magnitude-intensity)/0.92)-8.65)*150;
        double semiMinorAxis2 = (Math.exp((3.57+1.23*magnitude-intensity)/0.86)-4.86)*150;

        List<Hospital> dbHospitals = hospitalMapper.selectHospitAffectPoints(
                assessmentDTO.getLongitude(),
                assessmentDTO.getLatitude(),
                semiMajorAxis1,
                semiMinorAxis1,
                semiMajorAxis2,
                semiMinorAxis2
        );
        List<FireFighter> dbFireFighters = fireFighterMapper.selectFireFighterPoints(
                assessmentDTO.getLongitude(),
                assessmentDTO.getLatitude()
        );
        List<StorePoints> dbStorePoints = storePointsMapper.selectStorePoints(
                assessmentDTO.getLongitude(),
                assessmentDTO.getLatitude()
        );
        List<EarthQuakeReportEntity.Hospital> reportHospitals = new ArrayList<>();
        for (com.ruoyi.system.domain.Hospital dbHospital : dbHospitals) {
            // 创建报告内部类的Hospital对象（注意：必须通过外部类实例创建，因为是非静态内部类）
            EarthQuakeReportEntity.Hospital reportHospital = reportEntity.new Hospital();

            // 赋值：数据库实体属性 -> 报告内部类属性（字段对应关系需根据实际需求调整）
            reportHospital.setHospitalName(dbHospital.getName()); // 医院名称
            reportHospital.setHospitalBeds(dbHospital.getBeds() != null ? dbHospital.getBeds().toString() : "0"); // 总床位（转String，匹配内部类字段类型）
            reportHospital.setHospitalAddress(dbHospital.getAddress()); // 医院地址
            reportHospital.setHospitalLevel(dbHospital.getLevel()); // 医院等级（如三级甲等）

            // 将转换后的对象加入报告列表
            reportHospitals.add(reportHospital);
        };
        List<EarthQuakeReportEntity.FireFighter> reportFireFighters = new ArrayList<>();
        for (com.ruoyi.system.entity.FireFighter dbFireFighter : dbFireFighters) {
            EarthQuakeReportEntity.FireFighter reportFireFighter = reportEntity.new FireFighter();
            reportFireFighter.setFireFighterName(dbFireFighter.getTeamName());
            reportFireFighter.setFireFighterType(dbFireFighter.getTeamType());
            reportFireFighter.setFireFighterAddress(dbFireFighter.getAddress());
            reportFireFighter.setFireFighterNum(dbFireFighter.getTeamNum().toString());

            reportFireFighters.add(reportFireFighter);
        }
        List<EarthQuakeReportEntity.StorePoint> reportStorePoints = new ArrayList<>();
        for (com.ruoyi.system.entity.StorePoints dbStorePoint : dbStorePoints) {
            EarthQuakeReportEntity.StorePoint reportStorePoint = reportEntity.new StorePoint();
            reportStorePoint.setStorePointName(dbStorePoint.getName());
            reportStorePoint.setStorePointAddress(dbStorePoint.getAddress());
            reportStorePoint.setStorePointNum(dbStorePoint.getVolume().toString());
            reportStorePoint.setStorePointDep(dbStorePoint.getDepartment());
            reportStorePoints.add(reportStorePoint);
        }
        reportEntity.setEarthQuakeHospital(reportHospitals);
        reportEntity.setEarthQuakeFireFighter(reportFireFighters);
        reportEntity.setEarthQuakeStorePoint(reportStorePoints);
        if (reportEntity.getEarthQuakeMagnitude()>= 7.0){
            reportEntity.setEarthQuakeEmergencyLevel("一级");
        }else if (reportEntity.getEarthQuakeMagnitude()>= 6.0){
            reportEntity.setEarthQuakeEmergencyLevel("二级");
        }else if (reportEntity.getEarthQuakeMagnitude()>= 5.0){
            reportEntity.setEarthQuakeEmergencyLevel("三级");
        }else {
            reportEntity.setEarthQuakeEmergencyLevel("四级");
        }
        return reportEntity;

    }

}
