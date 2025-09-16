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
import com.ruoyi.system.mapper.AssessmentOutputMapper;
import com.ruoyi.system.mapper.HospitalMapper;
import com.ruoyi.system.mapper.RainAssessmentOutputMapper;
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
        // 6级及以上地震生成 灾情报告 和 辅助决策报告
        if (assessmentDTO.getMagnitude() >= BaseConstants.SEISMIC_6_GRADE) {
            // TODO 灾情报告与辅助决策报告
            EarthQuakeReportEntity reportEntity = new EarthQuakeReportEntity();
            reportEntity = getEarthquakeEntity(assessmentDTO);
            EqParams eqParams = new EqParams();
            eqParams.setEqId(assessmentDTO.getEqId());
            eqParams.setEqqueueId(assessmentDTO.getEqqueueId());
            System.out.println(eqParams);
            //获取图片
            List<AssessmentOutputDTO> earthquakeGraphs = getMap(eqParams);

            //获取图片达到15张时，进入循环，执行一次跳出循环（需要优化）
            while (earthquakeGraphs.size()>=15) {
                for (AssessmentOutputDTO earthquakeDTO : earthquakeGraphs) {
                    if (Objects.equals(earthquakeDTO.getFileName(), "震区附近医院分布图")){
                        reportEntity.setEarthQuakeHospitalGraph(earthquakeDTO.getSourceFile());
                    }
                    if (Objects.equals(earthquakeDTO.getFileName(), "影响估计范围分布图")){
                        reportEntity.setEarthQuakeInfluenceGraph(earthquakeDTO.getSourceFile());
                    }
                    if (Objects.equals(earthquakeDTO.getFileName(), "震区附近断层分布图")){
                        reportEntity.setEarthQuakeFaultZoneGraph(earthquakeDTO.getSourceFile());
                    }
                }
                try {
                    earthQuakeService.generateEarthQuakeReport(reportEntity);
                    log.info("报告生成完毕...");
                } catch (IOException | InvalidFormatException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        // 6级以下地震做辅助决策报告
        if (assessmentDTO.getMagnitude() < BaseConstants.SEISMIC_6_GRADE)
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
        // 累计降雨量高于 30mm 触发专题图
        if (Integer.parseInt(assessmentDTO.getRainfall()) > 30) {
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
        }
        reportEntity.setEarthQuakeHospital(reportHospitals);
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
