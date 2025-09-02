package com.ruoyi.system.core.drawers;

import com.ruoyi.common.core.domain.AssessmentBTO;
import com.ruoyi.common.drawers.report.*;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.domain.*;
import com.ruoyi.system.domain.bo.PopulationDensityBO;
import com.ruoyi.system.domain.bo.SeismicAfterRecommendationBO;
import com.ruoyi.system.domain.bo.SeismicCategoryBO;
import com.ruoyi.system.domain.bo.SpatiallyTimeAdviceBO;
import com.ruoyi.system.domain.dto.AssessmentDTO;
import com.ruoyi.system.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 9:18
 * @description: 辅助决策报告
 */


@Slf4j
@Service
public class SeismicReportsPrepareService {

    @Resource
    private YaResidentPopulationDensityMapper yaResidentPopulationDensityMapper;
    @Resource
    private YaVillageCommunityMapper yaVillageCommunityMapper;
    @Resource
    private YaVillagesMapper yaVillagesMapper;
    @Resource
    private AssessmentResultMapper assessmentResultMapper;
    @Resource
    private YaAdministrativeBoundaryMapper yaAdministrativeBoundaryMapper;
    @Resource
    private YaCountyTownMapper yaCountyTownMapper;
    @Resource
    private YaProvinceCityMapper yaProvinceCityMapper;

    /**
     * @param dto 评估参数
     * @author: xiaodemos
     * @date: 2025/5/16 20:03
     * @description: 地震应急辅助决策信息一
     * @return:
     */
    public void seismicEmergencyAssistDecisionInfo1(AssessmentDTO dto) {

    }


    /**
     * @param dto 评估参数
     * @author: xiaodemos
     * @date: 2025/5/12 10:42
     * @description: 地震应急辅助决策信息二
     * @return:
     */
    @Async("taskExecutor")
    public void seismicEmergencyAssistDecisionInfo2(AssessmentDTO dto) {

        // 抛出异常
        if (dto == null) {
            throw new RuntimeException("参数不能为空");
        }

        log.info("地震信息参数{}", dto);

        try {

            // 格式化时间
            String eqTime = dto.getEqTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时mm分ss秒"));
            // 设置文档标题
            String eqName = dto.getEqName().replace("undefined", "");
            String title = eqName + "发生" + dto.getMagnitude() + "级地震";


            // 计算各个地区的QX值,进行余震结论判断
            String aftershockConclusion = calculateQXValue(dto.getLongitude(), dto.getLatitude());
            // 雅安市人口密度
            PopulationDensityBO populationDensityBO = populationDensityFunction(dto, aftershockConclusion);
            // 获取震后影响损失情况
            // String fuJinTownResult = getAfterSeismicLoss(dto.getEqId());
            // 地震类别情况
            SeismicCategoryBO seismicCategoryBO = getSeismicCategory(dto, populationDensityBO);
            // 震后应急响应建议
            SeismicAfterRecommendationBO seismicAfterRecommendationBO = getSeisimicAfterRecommendation(dto, seismicCategoryBO, populationDensityBO);
            // 特殊时段处置建议
            SpatiallyTimeAdviceBO spatiallyTimeAdvice = getSpatiallyTimeAdvice(dto, populationDensityBO, seismicAfterRecommendationBO, seismicCategoryBO);
            // 应急处置建议拼接
            String filePath = mergeResponseAdvice(populationDensityBO, seismicCategoryBO, seismicAfterRecommendationBO, spatiallyTimeAdvice, title, eqTime, dto);

            // TODO 传入 rabbitmq 队列
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String mergeResponseAdvice(PopulationDensityBO populationDensityBO, SeismicCategoryBO seismicCategoryBO,
                                       SeismicAfterRecommendationBO seismicAfterRecommendationBO, SpatiallyTimeAdviceBO spatiallyTimeAdviceBO,
                                       String title, String eqTime, AssessmentDTO params) throws IOException {

        AssessmentBTO dto = new AssessmentBTO();
        // 对象转换
        BeanUtils.copyProperties(params, dto);

        // 合并所有字符串
        String combinedResult1 = populationDensityBO.getResult() + null + seismicCategoryBO.getPanduan()
                + seismicAfterRecommendationBO.getJianyi() + spatiallyTimeAdviceBO.getCuoshi();
        System.out.println("合并字段完成：" + combinedResult1);
        System.out.println("-----------------------------将要开始进行书写word文档阶段-----------------------");
        String filePath = GenerateReportFiles.WordExporter2(title, populationDensityBO.getResult(),
                null, seismicCategoryBO.getPanduan(),
                seismicAfterRecommendationBO.getJianyi(), spatiallyTimeAdviceBO.getCuoshi(), eqTime, dto);

        return filePath;
    }

    private SpatiallyTimeAdviceBO getSpatiallyTimeAdvice(AssessmentDTO dto, PopulationDensityBO populationDensityBO,
                                                         SeismicAfterRecommendationBO seismicAfterRecommendationBO,
                                                         SeismicCategoryBO seismicCategoryBO) {

        SpatiallyTimeAdviceBO spatiallyTimeAdviceBO = new SpatiallyTimeAdviceBO();

        //处置机构  G31
        // 查询雅安市所有乡镇（yaan_villages表）
        List<YaVillages> villageList1 = yaVillagesMapper.selectList(null);

        // 初始化最小距离
        double minVillageDistance1 = Double.MAX_VALUE;
        String nearestVillage1 = "";

        // 遍历所有乡镇，计算与上传经纬度的距离
        for (YaVillages village : villageList1) {
            Geometry geom = village.getGeom();
            if (geom != null && geom instanceof Point) {
                // 获取乡镇的经纬度
                Point villagePoint = (Point) geom;
                double villageLat = villagePoint.getY(); // 纬度
                double villageLon = villagePoint.getX(); // 经度

                // 计算震中到乡镇点的距离（单位：米）
                double distanceToVillage1 = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), villageLat, villageLon);

                // 计算震中到乡镇点的距离（单位：米）

                // 更新最小距离
                if (distanceToVillage1 < minVillageDistance1) {
                    minVillageDistance1 = distanceToVillage1;
                    nearestVillage1 = village.getVillagesName();  // 获取最近的乡镇名称
                }
            }
        }

        System.out.println("最近的雅安政府乡镇名称" + nearestVillage1);    //????不太确定YaanVillages表名，数据库没开


        //String  countyOrDistrict  县/区   输出eg:泸定县  F26  L3
        //String  cityOrState   市/州   输出eg:甘孜州   C26  k3
        //String  nearestVillage1  最近的雅安政府乡镇名称  输出eg:石棉县草科藏族乡  //G7


        spatiallyTimeAdviceBO.setMeasure("");
        // 将字符串转换为数字类型
        double populationDensityValue = Double.parseDouble(populationDensityBO.getPopulationDensity());

        System.out.println("字符串转换为数字类型建议:人口密度 populationDensityValue  " + populationDensityValue);

        if (dto.getEqName().contains("雅安市")) {
            // 判断人口密度和震级
            if (populationDensityValue >= 200) {
                if (dto.getMagnitude() < 5.0) {
                    spatiallyTimeAdviceBO.setMeasure("以" + populationDensityBO.getCountyOrDistrict() + "政府为主开展应急处置");
                } else if (dto.getMagnitude() >= 5.0 && dto.getMagnitude() < 6.0) {
                    spatiallyTimeAdviceBO.setMeasure("以" + populationDensityBO.getCityOrState() + "政府为主开展应急处置");
                } else if (dto.getMagnitude() >= 6.0) {
                    spatiallyTimeAdviceBO.setMeasure("以省政府为主开展应急处置，并接受省抗震救灾指挥部的领导与指挥");
                } else {
                    spatiallyTimeAdviceBO.setMeasure("无此应急响应情况，未定义应急处置建议");   //??
                }
            } else {
                if (dto.getMagnitude() < 5.5) {
                    spatiallyTimeAdviceBO.setMeasure("以" + populationDensityBO.getCountyOrDistrict() + "政府为主开展应急处置");
                } else if (dto.getMagnitude() >= 5.5 && dto.getMagnitude() < 6.5) {
                    spatiallyTimeAdviceBO.setMeasure("以" + populationDensityBO.getCityOrState() + "政府为主开展应急处置");
                } else if (dto.getMagnitude() >= 6.5) {
                    spatiallyTimeAdviceBO.setMeasure("以省政府为主开展应急处置，并接受省抗震救灾指挥部的领导与指挥");
                } else {
                    spatiallyTimeAdviceBO.setMeasure("无此应急响应情况，未定义应急处置建议");  //??
                }
            }
        } else {
            System.out.println("Suggestion: " + seismicAfterRecommendationBO.getSuggestion()); // 调试信息，查看 suggestion 值
            // 针对非雅安市
            switch (seismicAfterRecommendationBO.getSuggestion()) {
                case "不启动地震应急响应":
                    spatiallyTimeAdviceBO.setMeasure("以震中政府为主开展应急处置");
                    break;
                case "启动外地地震应急响应":
                    spatiallyTimeAdviceBO.setMeasure("以" + populationDensityBO.getCountyOrDistrict() + "政府为主开展应急处置");
                    break;
                case "启动强有感地震应急响应":
                case "启动市级地震灾害三级应急响应":
                    spatiallyTimeAdviceBO.setMeasure("以" + String.valueOf(minVillageDistance1).substring(0, 3) + "政府为主开展应急处置");
                    break;
                case "启动市级地震灾害二级应急响应":
                    spatiallyTimeAdviceBO.setMeasure("以雅安市政府为主开展应急处置");
                    break;
                case "启动市级地震灾害一级应急响应":
                    spatiallyTimeAdviceBO.setMeasure("以省政府为主开展应急处置，并接受省抗震救灾指挥部的领导与指挥");
                    break;
                case "应急响应无符合启动条件":
                    spatiallyTimeAdviceBO.setMeasure("应急响应无符合启动条件，未定义应急处置建议");
                    break;
                default:
                    spatiallyTimeAdviceBO.setMeasure("无此应急响应情况，未定义应急处置建议");
            }
        }

        // 输出结果
        System.out.println("应急措施: " + spatiallyTimeAdviceBO.getMeasure());   //G31

        //1.指挥部建议  H31

        String earthquakeName = "“" + populationDensityBO.getMonthDay() + "”" + populationDensityBO.getNewCountyOrDistrict() + dto.getMagnitude() + "级地震";  // N29

        spatiallyTimeAdviceBO.setHeadquarters(EmergencyResponseDecider.earthquakeResponse(populationDensityBO.getCityOrState(), seismicAfterRecommendationBO.getSuggestion(), populationDensityBO.getCountyOrDistrict(), earthquakeName));

        System.out.println("1.指挥部建议 :" + spatiallyTimeAdviceBO.getHeadquarters());  // 输出相应的响应信息

        //2.灾情收集建议  I31  放在4下面，因为4有maximumIntensityPoint  **

        //3.应急支援建议  J31

        spatiallyTimeAdviceBO.setSupport(EmergencyResponseDecider.supportFunction(populationDensityBO.getCityOrState(), dto.getMagnitude(), populationDensityBO.getCityOrState(), seismicAfterRecommendationBO.getSuggestion()));


        System.out.println("3.应急支援建议 :" + spatiallyTimeAdviceBO.getSupport());  // 输出相应的响应信息

        //4.交通处置建议  K31


        //(1) I29 有破坏县区（6-12度）

        // 模拟查询雅安市政府相关的县区镇（yaan_county_town表）
        List<YaCountyTown> countyTownList1 = yaCountyTownMapper.selectList(null);  //????不太确定YaanCountyTown表名，数据库没开

        // 用 Map 存储所有政府县区名称及其对应的震中距
        Map<String, Double> countyTownDistances = new LinkedHashMap<>();

        // 遍历所有政府相关的乡镇，计算与上传经纬度的距离
        for (YaCountyTown countyTown : countyTownList1) {
            Geometry geom = countyTown.getGeom();
            if (geom != null && geom instanceof Point) {
                // 获取政府县区的经纬度
                Point countyTownPoint = (Point) geom;
                double countyTownLat = countyTownPoint.getY(); // 纬度
                double countyTownLon = countyTownPoint.getX(); // 经度

                // 计算震中到县区点的距离（单位：米）
                double distanceToCountyTown = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), countyTownLat, countyTownLon);

                // 存储县区名称及其震中距
                countyTownDistances.put(countyTown.getCountyTownName(), distanceToCountyTown);
            }
        }

        // **输出所有县区的名称和对应的震中距离**
        for (Map.Entry<String, Double> entry : countyTownDistances.entrySet()) {
            System.out.println("县区名称: " + entry.getKey() + "，震中距: " + entry.getValue() + " 米");
        }

        // 存储乡镇名称、震中距和计算后的烈度
        Map<String, Double> intensities = new LinkedHashMap<>();

        // 遍历计算每个乡镇的烈度
        for (Map.Entry<String, Double> entry : countyTownDistances.entrySet()) {
            String townName = entry.getKey();  // 乡镇名称
            double distance = entry.getValue(); // 震中距（D）

            // 计算 X 的值
            double X = (dto.getMagnitude() > 5.5) ? 0.4 : 0.6;

            // 计算烈度 I
            double intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(distance + 24)) - X;

            // 确保烈度不小于 0
            intensity = Math.max(intensity, 0);

            // 将烈度值四舍五入为整数
            intensity = Math.round(intensity);

            // 存入 Map
            intensities.put(townName, intensity);
        }

        // **输出每个乡镇的烈度**
        for (Map.Entry<String, Double> entry : intensities.entrySet()) {
            System.out.println("县区名称: " + entry.getKey() + "，震中距: " + countyTownDistances.get(entry.getKey()) + " km，烈度: " + String.format("%.2f", entry.getValue()));
        }

        // **将 Map 转换成 List 并排序**
        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(intensities.entrySet());
        sortedList.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())); // 按烈度降序排序

        // **输出排序后的乡镇烈度信息**
        System.out.println("县区名称 | 烈度");
        System.out.println("--------------------");
        for (Map.Entry<String, Double> entry : sortedList) {
            System.out.printf("%s | %.2f%n", entry.getKey(), entry.getValue());
        }

        String destroy = EmergencyResponseDecider.generateDestroy(sortedList);
        System.out.println("有破坏县区（6-12度）：" + destroy);  // 输出相应的响应信息

        //(2) J29 最大烈度点

        // 查询雅安市所有乡镇
        List<YaVillages> villageList = yaVillagesMapper.selectList(null); //????不太确定YaanVillages表名，数据库没开

        // 用 Map 存储所有乡镇名称及其对应的震中距
        Map<String, Double> villageDistances = new HashMap<>();

        // 遍历所有乡镇，计算与上传经纬度的距离
        for (YaVillages village : villageList) {
            Geometry geom = village.getGeom();
            if (geom != null && geom instanceof Point) {
                // 获取乡镇的经纬度
                Point villagePoint = (Point) geom;
                double villageLat = villagePoint.getY(); // 纬度
                double villageLon = villagePoint.getX(); // 经度

                // 计算震中到乡镇点的距离（单位：米）
                double distanceToVillage = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), villageLat, villageLon);

                // 存储乡镇名称及其震中距
                villageDistances.put(village.getVillagesName(), distanceToVillage);
            }
        }

        // **对乡镇按照震中距从小到大排序，并取前 8 个**
        List<Map.Entry<String, Double>> sortedVillages = new ArrayList<>(villageDistances.entrySet());
        sortedVillages.sort(Map.Entry.comparingByValue()); // 按震中距升序排序

        // 取前 8 个乡镇
        int limit = Math.min(8, sortedVillages.size()); // 防止乡镇数量不足 8 个
        List<Map.Entry<String, Double>> top8Villages = sortedVillages.subList(0, limit);

        // **输出前 8 个乡镇及其震中距**
        System.out.println("最近的 8 个乡镇及其震中距：");
        for (Map.Entry<String, Double> entry : top8Villages) {
            System.out.println("乡镇名称: " + entry.getKey() + "，震中距: " + entry.getValue() + " 米");
        }


        // 存储乡镇名称、震中距和计算后的烈度
        Map<String, Double> intensities2 = new LinkedHashMap<>();

        // 遍历计算每个乡镇的烈度
        for (Map.Entry<String, Double> entry : villageDistances.entrySet()) {
            String townName = entry.getKey();  // 乡镇名称
            double distance = entry.getValue(); // 震中距（D）

            // 计算 X 的值
            double X = (dto.getMagnitude() > 5.5) ? 0.4 : 0.6;

            // 计算烈度 I
            double intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(distance + 24)) - X;

            // 确保烈度不小于 0
            intensity = Math.max(intensity, 0);

            // 将烈度值四舍五入为整数
            intensity = Math.round(intensity);

            // 存入 Map
            intensities2.put(townName, intensity);
        }

        // **输出每个乡镇的烈度**
        for (Map.Entry<String, Double> entry : intensities2.entrySet()) {
            System.out.println("乡镇名称: " + entry.getKey() + "，震中距: " + villageDistances.get(entry.getKey()) + " km，烈度: " + String.format("%.2f", entry.getValue()));
        }

        // **将 Map 转换成 List 并排序**
        List<Map.Entry<String, Double>> sortedList2 = new ArrayList<>(intensities2.entrySet());
        sortedList2.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())); // 按烈度降序排序

        // **输出排序后的乡镇烈度信息**
        System.out.println("乡镇名称 | 烈度");
        System.out.println("--------------------");
        for (Map.Entry<String, Double> entry : sortedList2) {
            System.out.printf("%s | %.2f%n", entry.getKey(), entry.getValue());
        }

        // 计算最大烈度点
        String maximumIntensityPoint = EmergencyResponseDecider.generateDestroy2(sortedList, sortedList2, seismicCategoryBO.getMaxIntensity());

        // 输出结果
        System.out.println("最大烈度点：" + maximumIntensityPoint);


        //(3) 开始 4.交通处置建议 逻辑

        //String cityOrState   为  C26
        //Double eqMagnitude 为 I3
        //String  cityOrState  为  K3
        //String  countyOrDistrict 为 F26
        //String suggestion 为  B31
        //String category 为  D31
        //String destroy 为  I29
        //String maximumIntensityPoint 为  J29


        spatiallyTimeAdviceBO.setTransportation(EmergencyResponseDecider.transportationFunction(populationDensityBO.getCityOrState(), dto.getMagnitude(), populationDensityBO.getCityOrState(),
                populationDensityBO.getCountyOrDistrict(), seismicAfterRecommendationBO.getSuggestion(),
                seismicCategoryBO.getCategory(), destroy, maximumIntensityPoint));


        System.out.println("4.交通处置建议:" + spatiallyTimeAdviceBO.getTransportation());  // 输出相应的响应信息

        //2.灾情收集建议  I31  放在4下面，因为4有maximumIntensityPoint  **

        spatiallyTimeAdviceBO.setDisasterCollection(EmergencyResponseDecider.disasterCollectionFunction(seismicCategoryBO.getMaxIntensityWithUnit(), maximumIntensityPoint));

        System.out.println("2.灾情收集建议 :" + spatiallyTimeAdviceBO.getDisasterCollection());  // 输出相应的响应信息

        //5.危险源处置建议  L31

        spatiallyTimeAdviceBO.setDangerSource(EmergencyResponseDecider.generateResponse(seismicCategoryBO.getMaxIntensityWithUnit()));
        System.out.println("5.危险源处置建议：" + spatiallyTimeAdviceBO.getDangerSource());  // 输出相应的响应信息

        //6.灾情公开建议  M31

        spatiallyTimeAdviceBO.setDisasterMadepublic("无");

        // 判断是否为市级地震灾害响应
        if (seismicAfterRecommendationBO.getSuggestion().equals("启动市级地震灾害三级应急响应") ||
                seismicAfterRecommendationBO.getSuggestion().equals("启动市级地震灾害二级应急响应") ||
                seismicAfterRecommendationBO.getSuggestion().equals("启动市级地震灾害一级应急响应")) {
            spatiallyTimeAdviceBO.setDisasterMadepublic("尽快向社会公开震情、灾情等，统筹“12345”政务服务便民热线，建立抗震救灾服务热线，及时回应抗震救灾相关咨询");
        } else {
            spatiallyTimeAdviceBO.setDisasterMadepublic("无");
        }

        System.out.println("6.灾情公开建议：" + spatiallyTimeAdviceBO.getDisasterMadepublic());  // 输出相应的响应信息

        //7.舆情处置建议  N31

        spatiallyTimeAdviceBO.setPublicOpinion("无");

        // 判断是否属于地震强度在3度及以上的情况
        if (seismicCategoryBO.getMaxIntensityWithUnit().equals("3度") || seismicCategoryBO.getMaxIntensityWithUnit().equals("4度") || seismicCategoryBO.getMaxIntensityWithUnit().equals("5度") ||
                seismicCategoryBO.getMaxIntensityWithUnit().equals("6度") || seismicCategoryBO.getMaxIntensityWithUnit().equals("7度") || seismicCategoryBO.getMaxIntensityWithUnit().equals("8度") ||
                seismicCategoryBO.getMaxIntensityWithUnit().equals("9度") || seismicCategoryBO.getMaxIntensityWithUnit().equals("10度") || seismicCategoryBO.getMaxIntensityWithUnit().equals("11度") ||
                seismicCategoryBO.getMaxIntensityWithUnit().equals("12度")) {
            spatiallyTimeAdviceBO.setPublicOpinion("做好舆情监控和引导工作，防止我市出现地震谣传");
        } else {
            spatiallyTimeAdviceBO.setPublicOpinion("无");
        }

        System.out.println("7.舆情处置建议：" + spatiallyTimeAdviceBO.getPublicOpinion());  // 输出相应的响应信息

        //8.人员伤亡和财产损失估算  O31

        String impactMessage = EmergencyResponseDecider.generateImpactMessage(seismicCategoryBO.getMaxIntensityWithUnit());
        System.out.println("8.人员伤亡和财产损失估算：" + impactMessage);  // 输出相应的影响信息

        // 9.特殊时段处置建议  （没有结果就是空的） P31

        //(1)是否特殊日期判断
        // 将整数类型的年、月、日转换为字符串
        String yearStr = String.valueOf(populationDensityBO.getYear());  //  B3  String
        String monthStr = String.format("%02d", populationDensityBO.getMonth());  // 格式化为两位数   //C3  String
        String dayStr = String.format("%02d", populationDensityBO.getDay());  // 格式化为两位数   //D3  String

        // 获取日期对应的事件  eg:国庆节   P29
        String events = SpecialEventsResponseHandle.getEventForDate(yearStr, monthStr, dayStr);

        //（2）开始特殊时段处置建议
        //int maxIntensity  为  B20
        //String maxIntensityWithUnit 加上 "度"的   B20
        // String cityOrState   为  C26
        //String events 为  P29
        // String suggestion 为  B31
        //Double eqMagnitude 为 I3

        spatiallyTimeAdviceBO.setAdvice(SpecialEventsResponseHandle.generateAdvice(populationDensityBO.getCityOrState(), events, seismicAfterRecommendationBO.getSuggestion(),
                seismicCategoryBO.getMaxIntensityWithUnit(), dto.getMagnitude()));   //P31
        System.out.println("9.特殊时段处置建议：" + spatiallyTimeAdviceBO.getAdvice());

        System.out.println("*****************************************");

        //----------应急处置建议开始拼接----------------------

        // 假设输入的各个单元格值
        // N31 - publicOpinion
        // M31- disasterMadepublic
        // L31- dangerSource
        // K31- transportation
        // J31- support
        // I31- disasterCollection
        // H31- headquarters
        // G31- measure
        // String P31 = advice


        // 调用处理方法
        String connect = EmergencyResponseDecider.generateSuggestion(spatiallyTimeAdviceBO.getAdvice(), spatiallyTimeAdviceBO.getMeasure(),
                spatiallyTimeAdviceBO.getHeadquarters(), spatiallyTimeAdviceBO.getDisasterCollection(),
                spatiallyTimeAdviceBO.getSupport(), spatiallyTimeAdviceBO.getTransportation(),
                spatiallyTimeAdviceBO.getDangerSource(), spatiallyTimeAdviceBO.getDisasterMadepublic(),
                spatiallyTimeAdviceBO.getPublicOpinion());
        System.out.println("**" + populationDensityBO.getResult());

        // 处置措施建议字符串
        spatiallyTimeAdviceBO.setCuoshi(String.format("处置措施建议：%s", connect));

        System.out.println("最终处置措施建议" + spatiallyTimeAdviceBO.getCuoshi());

        return spatiallyTimeAdviceBO;
    }

    private SeismicAfterRecommendationBO getSeisimicAfterRecommendation(AssessmentDTO dto, SeismicCategoryBO seismicCategoryBO, PopulationDensityBO populationDensityBO) {

        SeismicAfterRecommendationBO seismicAfterRecommendationBO = new SeismicAfterRecommendationBO();
        seismicAfterRecommendationBO.setSuggestion("");

        // 判断 eqName 是否包含 "雅安市"
        if (dto.getEqName().contains("雅安市")) {
            // 针对雅安市的逻辑
            if (dto.getMagnitude() < 3.5) {
                seismicAfterRecommendationBO.setSuggestion("不启动地震应急响应");
            } else if ((dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 3.5 && dto.getMagnitude() < 4.0) {

                seismicAfterRecommendationBO.setSuggestion("启动强有感地震应急响应");
            } else if (!(dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 3.5 && dto.getMagnitude() < 4.5) {
                seismicAfterRecommendationBO.setSuggestion("启动强有感地震应急响应");
            } else if ((dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 4.0 && dto.getMagnitude() < 5.0) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害三级应急响应");
            } else if (!(dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 4.5 && dto.getMagnitude() < 5.5) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害三级应急响应");
            } else if ((dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 5.0 && dto.getMagnitude() < 6.0) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害二级应急响应");
            } else if (!(dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 5.5 && dto.getMagnitude() < 6.5) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害二级应急响应");
            } else if ((dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 6.0) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害一级应急响应");
            } else if (!(dto.getEqAddr().contains("雨城区") || dto.getEqAddr().contains("名山区")) && dto.getMagnitude() >= 6.5) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害一级应急响应");
            } else {
                seismicAfterRecommendationBO.setSuggestion("应急响应无符合启动条件");
            }

        } else {
            // 判断 eqName 不包含 "雅安市"
            if (seismicCategoryBO.getOutside() < 5) {
                if (dto.getMagnitude() < 5.0) {
                    seismicAfterRecommendationBO.setSuggestion("不启动地震应急响应");
                } else {
                    seismicAfterRecommendationBO.setSuggestion("启动外地地震应急响应");
                }
            } else if ((seismicCategoryBO.getOutside() == 6.0 && seismicCategoryBO.getBig() == 0.0) || seismicCategoryBO.getOutside() == 5.0 || seismicCategoryBO.getBig() == 1.0) {
                seismicAfterRecommendationBO.setSuggestion("启动强有感地震应急响应");
            } else if (seismicCategoryBO.getBig() >= 2) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害一级应急响应");
            } else if (seismicCategoryBO.getMiddle() >= 2) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害二级应急响应");
            } else if (seismicCategoryBO.getSmall() >= 2) {
                seismicAfterRecommendationBO.setSuggestion("启动市级地震灾害三级应急响应");
            } else {
                seismicAfterRecommendationBO.setSuggestion("应急响应无符合启动条件");
            }

            // 处理大、中、小区域的响应

        }

        // 输出建议
        System.out.println("应急响应建议: " + seismicAfterRecommendationBO.getSuggestion());

        String response;

        if ("不启动地震应急响应".equals(seismicAfterRecommendationBO.getSuggestion())) {
            response = " ";
        } else {
            // 去掉suggestion中的“启动”
            String suggestionWithoutStart = seismicAfterRecommendationBO.getSuggestion().replace("启动", "");
            response = "建议按照《雅安市地震应急预案》" + suggestionWithoutStart + "处置措施开展应急处置。";
        }
        System.out.println("响应:" + response);  // Output: 按照《雅安市地震应急预案》市级地震灾害一级应急响应处置措施开展应急处置


        // 计算应急响应的详细内容
        seismicAfterRecommendationBO.setPlan(EmergencyResponseDecider.generateEarthquakeResponse(populationDensityBO.getCityOrState(), seismicAfterRecommendationBO.getSuggestion(),
                seismicCategoryBO.getOutside(), seismicCategoryBO.getBig(), seismicCategoryBO.getMiddle(), seismicCategoryBO.getSmall(),
                dto.getMagnitude(), populationDensityBO.getCountyOrDistrict(), populationDensityBO.getPopulationDensity()));


        // 检查 maxIntensity 是否小于 0，如果是，设置为 0
        if (seismicCategoryBO.getMaxIntensity() < 0) {
            seismicCategoryBO.setMaxIntensity(0);
        }

        System.out.println("最终的最大烈度是：" + seismicCategoryBO.getMaxIntensity());

        // 生成应急响应建议
        // String jianyi;
        if ("不启动地震应急响应".equals(seismicAfterRecommendationBO.getSuggestion())) {
            seismicAfterRecommendationBO.setJianyi(String.format(
                    "应急响应建议：建议我市不启动地震应急响应（主要依据：1、%s；2、震级:%.1f级；3、我市最大烈度：%d度。）",
                    seismicCategoryBO.getCategory(), dto.getMagnitude(), seismicCategoryBO.getMaxIntensity()
            ));
        } else {
            seismicAfterRecommendationBO.setJianyi(String.format(
                    "应急响应建议：建议我市%s，主要依据：1、%s；%s%s",
                    seismicAfterRecommendationBO.getSuggestion(), seismicCategoryBO.getCategory(), seismicAfterRecommendationBO.getPlan(), response
            ));

        }

        // 输出结果
        System.out.println("最终应急响应建议: " + seismicAfterRecommendationBO.getJianyi());

        return seismicAfterRecommendationBO;
    }

    private SeismicCategoryBO getSeismicCategory(AssessmentDTO dto, PopulationDensityBO populationDensityBO) {

        //------------- 定义 类别(category) 变量-----------
//        String category;  //D31

        SeismicCategoryBO seismicCategoryBO = new SeismicCategoryBO();

        // 判断 eqName 对应的字段
        if (dto.getEqName().contains("雅安市") ||
                dto.getEqName().contains("名山区") ||
                dto.getEqName().contains("宝兴县") ||
                dto.getEqName().contains("雨城区") ||
                dto.getEqName().contains("芦山县") ||
                dto.getEqName().contains("天全县") ||
                dto.getEqName().contains("荥经县") ||
                dto.getEqName().contains("汉源县") ||
                dto.getEqName().contains("石棉县")) {
            seismicCategoryBO.setCategory("雅安本地地震");
            ;
        } else if (dto.getEqName().contains("阿坝州") || dto.getEqName().contains("甘孜州") || dto.getEqName().contains("眉山市") ||
                dto.getEqName().contains("成都市") || dto.getEqName().contains("凉山州") || dto.getEqName().contains("乐山市")) {
            seismicCategoryBO.setCategory("邻近市州地震");
            ;
        } else {
            seismicCategoryBO.setCategory("外地地震");  // 其他情况归为外地地震   //？？？
        }

        System.out.println("属于哪个位置的地震（category）:" + seismicCategoryBO.getCategory());
        //------------判断------------------

//        int maxIntensity = 0;   //  最大烈度 初始为0       B20 无"度"字
//        int outside = 0;//   辖区外地震雅安最大烈度     F19
//
//        //如果是不是雅安市内的
//        double big = 0; //外地地震雅安最大烈度8度及以上时，筛选7度及以上县个数   J26
//        double middle = 0; //外地地震雅安最大烈度7度及以上时，筛选6度、7度县个数  K26
//        double small = 0;  //外地地震雅安最大烈度6度及以上时，筛选5度、6度县个数   L26
        seismicCategoryBO.setMaxIntensity(0);
        seismicCategoryBO.setBig(0);
        seismicCategoryBO.setMiddle(0);
        seismicCategoryBO.setSmall(0);
        seismicCategoryBO.setOutside(0);

        //震中附近乡镇计算
        if (dto.getEqName().contains("四川")) {

            if (dto.getEqAddr().contains("雅安市") ||
                    dto.getEqAddr().contains("名山区") ||
                    dto.getEqAddr().contains("宝兴县") ||
                    dto.getEqAddr().contains("雨城区") ||
                    dto.getEqAddr().contains("芦山县") ||
                    dto.getEqAddr().contains("天全县") ||
                    dto.getEqAddr().contains("荥经县") ||
                    dto.getEqAddr().contains("汉源县") ||
                    dto.getEqAddr().contains("石棉县")) {
                //显示文字，震中距离雅安市边界约distance公里，就将上传的经纬度latitude, longitude和雅安市行政边界表yaan_administrative_boundary计算每个震中距离找出最小值

                System.out.println("------------------eqAddr内有雅安市，进入雅安市的烈度计算---------------------:" + dto.getEqAddr());
                // ------------计算震中距离雅安市边界的最小距离----------------

                // 查询雅安市行政边界的所有点或多边形（geom字段）
                List<YaAdministrativeBoundary> boundaryList = yaAdministrativeBoundaryMapper.selectList(null);

                // 初始化最小距离
                double minBoundaryDistance = Double.MAX_VALUE;

                // 遍历所有边界点或多边形，计算震中到边界的最小距离
                for (YaAdministrativeBoundary boundary : boundaryList) {
                    Geometry boundaryGeom = boundary.getGeom();
                    if (boundaryGeom != null) {
                        if (boundaryGeom instanceof Point) {
                            // 点到点的距离
                            Point boundaryPoint = (Point) boundaryGeom;

                            double boundaryLat = boundaryPoint.getY(); // 纬度
                            double boundaryLon = boundaryPoint.getX(); // 经度

                            // 计算震中到乡镇点的距离（单位：米）
                            double distanceToCountyTown = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), boundaryLat, boundaryLon);
                            // 更新最小距离
                            if (distanceToCountyTown < minBoundaryDistance) {
                                minBoundaryDistance = distanceToCountyTown;
                            }
                        }
                    }
                }

                // 计算后的最小距离（单位：公里）
                double minDistanceInKm = minBoundaryDistance;  // 转换为公里


                //----------------------震中到乡镇点的距离--------------
                // 查询雅安市所有乡镇（yaan_villages表）
                List<YaVillages> villageList = yaVillagesMapper.selectList(null);

                // 初始化最小距离（公里）
                double minVillageDistance = Double.MAX_VALUE;
//                String nearestVillage = "";

                // 遍历所有乡镇，计算与上传经纬度的距离
                for (YaVillages village : villageList) {
                    Geometry geom = village.getGeom();
                    if (geom != null && geom instanceof Point) {
                        // 获取乡镇的经纬度
                        Point villagePoint = (Point) geom;
                        double villageLat = villagePoint.getY(); // 纬度
                        double villageLon = villagePoint.getX(); // 经度

                        // 计算震中到乡镇点的距离（单位：米）
                        double distanceToVillage = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), villageLat, villageLon);

                        // 更新最小距离
                        if (distanceToVillage < minVillageDistance) {
                            minVillageDistance = distanceToVillage;
                        }
                    }
                }


                System.out.println("计算取整后辖区外地震雅安最大烈度roundedIntensity1所用距离为:  ");
                System.out.println("距离雅安市边界的最小距离 minDistanceInKm: " + minDistanceInKm);
                System.out.println("计算取整后最大烈度（烈度衰减公式长轴计算结果）minVillageDistance所用距离为: ");
                System.out.println("震中到乡镇点的最小距离 roundedIntensity2:" + minVillageDistance);

                //*****************************************************

                //初步评估
                // 公式部分

                double intensity;  //震中点最大烈度（烈度衰减长轴计算结果）
                double intensity1; //辖区外地震雅安最大烈度
                double intensity2; //最大烈度（烈度衰减公式长轴计算结果）
                //主要受影响区域
                if (dto.getMagnitude() > 5.5) {
                    // 震级大于5.5时使用这个公式
                    intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(0.1 + 24)) - 0.4;
                    intensity1 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minDistanceInKm + 24)) - 0.4;
                    intensity2 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minVillageDistance + 24)) - 0.4;

                } else {
                    // 震级小于或等于5.5时使用这个公式
                    intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(0.1 + 24)) - 0.6;
                    intensity1 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minDistanceInKm + 24)) - 0.6;
                    intensity2 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minVillageDistance + 24)) - 0.6;
                }

                // 打印变量的值
                System.out.println("震中点最大烈度（烈度衰减长轴计算结果）: " + intensity);
                System.out.println("辖区外地震雅安最大烈度: " + intensity1);
                System.out.println("最大烈度（烈度衰减公式长轴计算结果）: " + intensity2);

                int roundedIntensity = (int) Math.round(intensity);
                int roundedIntensity1 = (int) Math.round(intensity1);
                int roundedIntensity2 = (int) Math.round(intensity2);

                // 打印变量的值
                System.out.println("取整后震中点最大烈度（烈度衰减长轴计算结果）: " + roundedIntensity);
                System.out.println("取整后辖区外地震雅安最大烈度: " + roundedIntensity1);
                System.out.println("取整后最大烈度（烈度衰减公式长轴计算结果）: " + roundedIntensity2);


                //得到我市最大烈度
                seismicCategoryBO.setMaxIntensity(roundedIntensity2);

                System.out.println("maxIntensity烈度选取：roundedIntensity2取整后最大烈度（烈度衰减公式长轴计算结果）: " + roundedIntensity2);

            }


            //不在雅安市地震
            if (!dto.getEqAddr().contains("雅安市")) {

                System.out.println("------------------eqAddr内没有雅安市，进入外地的烈度计算---------------------:" + dto.getEqAddr());


                // ------------计算震中距离雅安市边界的最小距离----------------


                // 查询雅安市行政边界的所有点或多边形（geom字段）
                List<YaAdministrativeBoundary> boundaryList1 = yaAdministrativeBoundaryMapper.selectList(null);

                // 初始化最小距离
                double minBoundaryDistance1 = Double.MAX_VALUE;

                // 遍历所有边界点或多边形，计算震中到边界的最小距离
                for (YaAdministrativeBoundary boundary : boundaryList1) {
                    Geometry boundaryGeom = boundary.getGeom();
                    if (boundaryGeom != null) {

                        if (boundaryGeom instanceof Point) {
                            // 点到点的距离
                            Point boundaryPoint = (Point) boundaryGeom;

                            double boundaryLat = boundaryPoint.getY(); // 纬度
                            double boundaryLon = boundaryPoint.getX(); // 经度

                            // 计算震中到边界的距离（单位：米）minBoundaryDistance1
                            double distanceToCountyTown1 = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), boundaryLat, boundaryLon);
                            // 更新最小距离
                            if (distanceToCountyTown1 < minBoundaryDistance1) {
                                minBoundaryDistance1 = distanceToCountyTown1;
                            }
                        }
                    }
                }
                // 计算后的最小距离（单位：公里）
                double minDistanceInKm1 = minBoundaryDistance1;  // 转换为公里

                System.out.println("距离雅安市边界的最小距离 minDistanceInKm1: " + minDistanceInKm1);


                //*******************----------------------尝试烈度-------------------
                /////////////////////-----------
                // 查询雅安市政府相关的乡镇（yaan_county_town表）
                List<YaCountyTown> countyTownList1 = yaCountyTownMapper.selectList(null);

                double minCountyTownDistance1 = Double.MAX_VALUE;
                String nearestCountyTown1 = "";

                // 特殊的政府乡镇距离变量
                double yaanCityGovDistance1 = Double.MAX_VALUE;
                double sichuanProvGovDistance1 = Double.MAX_VALUE;

                // 遍历所有政府相关的乡镇，计算与上传经纬度的距离
                for (YaCountyTown countyTown : countyTownList1) {
                    Geometry geom = countyTown.getGeom();
                    if (geom != null && geom instanceof Point) {
                        // 获取政府乡镇的经纬度
                        Point countyTownPoint = (Point) geom;
                        // 提取经纬度
                        double countyTownLat = countyTownPoint.getY(); // 纬度
                        double countyTownLon = countyTownPoint.getX(); // 经度

                        // 计算震中到乡镇点的距离（单位：米）
                        double distanceToCountyTown1 = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), countyTownLat, countyTownLon);

                        // 单独处理雅安市政府和四川省政府的情况
                        if ("雅安市政府".equals(countyTown.getCountyTownName())) {
                            yaanCityGovDistance1 = distanceToCountyTown1; // 转换为公里
                        } else if ("四川省政府".equals(countyTown.getCountyTownName())) {
                            sichuanProvGovDistance1 = distanceToCountyTown1; // 转换为公里
                        } else {
                            // 正常更新最近的政府乡镇距离和名称  minCountyTownDistance1
                            if (distanceToCountyTown1 < minCountyTownDistance1) {
                                minCountyTownDistance1 = distanceToCountyTown1;
                                nearestCountyTown1 = countyTown.getCountyTownName(); // 获取最近的政府乡镇名称
                            }
                        }
                    }
                }

//                // 生成最终输出文字
//                String fuJinCountyTownResult1 = String.format(
//                        "距离雅安市"+nearestCountyTown1+"约%.1f公里，距离雅安市政府约%.1f公里，距离四川省政府约%.1f公里。",
//                        minCountyTownDistance1, yaanCityGovDistance1, sichuanProvGovDistance1
//                );
//                // 生成返回的政府乡镇距离结果
//                System.out.println(fuJinCountyTownResult1);

                //-------------------------------------------

                // 查询所有城市的经纬度（geom字段）
                List<YaProvinceCity> cityList1 = yaProvinceCityMapper.selectList(null);

                // 初始化最小距离
                double minCityDistance1 = Double.MAX_VALUE;
                String nearestCity1 = "";

                // 遍历所有城市，计算与上传经纬度的距离
                for (YaProvinceCity city : cityList1) {
                    Geometry geom = city.getGeom();
                    System.out.println("geom:" + geom);
                    if (geom != null && geom instanceof Point) {
                        // 获取城市的经纬度
                        Point cityPoint = (Point) geom;

                        // 提取经纬度
                        double cityLat = cityPoint.getY(); // 纬度
                        double cityLon = cityPoint.getX(); // 经度

                        // 计算震中到城市点的距离（单位：米） minCityDistance1
                        double distanceToCity1 = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), cityLat, cityLon);
                        System.out.println("距离: " + distanceToCity1);

                        // 更新最小距离
                        if (distanceToCity1 < minCityDistance1) {
                            minCityDistance1 = distanceToCity1;
                            nearestCity1 = city.getFullProvinceCityName();  // 获取最近的城市名称
                        }
                    }
                }

                // 计算后的最小距离（单位：公里）
                double minCityDistanceInKm1 = minCityDistance1;// 转换为公里
//                System.out.println("最小距离（公里）：" + minCityDistanceInKm1);
//
//                // 生成返回的城市距离结果
//                String fuJinCityResult1 = String.format("距离"+ nearestCity1 +"政府约%.2f公里，", minCityDistanceInKm1);
//                System.out.println(fuJinCityResult1);


                // 查询雅安市所有乡镇（yaan_villages表）
                List<YaVillages> villageList = yaVillagesMapper.selectList(null);

                // 初始化最小距离
                double minVillageDistance = Double.MAX_VALUE;
                String nearestVillage = "";
                for (YaVillages village : villageList) {
                    Geometry geom = village.getGeom();
                    if (geom != null && geom instanceof Point) {
                        // 获取乡镇的经纬度
                        Point villagePoint = (Point) geom;
                        double villageLat = villagePoint.getY(); // 纬度
                        double villageLon = villagePoint.getX(); // 经度

                        // 计算震中到乡镇点的距离（单位：米） minVillageDistance
                        double distanceToVillage = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), villageLat, villageLon);

                        // 更新最小距离
                        if (distanceToVillage < minVillageDistance) {
                            minVillageDistance = distanceToVillage;
                            nearestVillage = village.getVillagesName();  // 获取最近的乡镇名称
                        }
                    }
                }


                System.out.println("取整后震中到城市点的最小距离minCityDistance1: " + minCityDistance1);  //震中区最大地震烈度达%d度
                System.out.println("取整后雅安市行政边界最小距离minDistanceInKm1: " + minDistanceInKm1);
                System.out.println("取整后最近的政府乡镇距离minCountyTownDistance1: " + minCountyTownDistance1);
                System.out.println("取整后雅安市所有乡镇距离minCountyTownDistance1: " + minVillageDistance);
                //*************-----------------------------------------------------


                double intensity;  //辖区外地震雅安最大烈度
                double intensity1;  //
                double intensity2; //
                double intensity3;

                //震级：dto.getMagnitude()
                //主要受影响区域
                if (dto.getMagnitude() > 5.5) {
                    // 震级大于5.5时使用这个公式
                    intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minCityDistance1 + 24)) - 0.4;  //计算震中到城市点的最小距离（单位：米） minCityDistance1
                    intensity1 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minDistanceInKm1 + 24)) - 0.4; //计算雅安市行政边界最小距离（单位：米）minBoundaryDistance1
                    intensity2 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minCountyTownDistance1 + 24)) - 0.4;  //正常更新最近的政府乡镇距离  minCountyTownDistance1
                    intensity3 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minVillageDistance + 24)) - 0.4;  //雅安市所有乡镇距离乡镇最小距离  minCountyTownDistance1

                } else {
                    // 震级小于或等于5.5时使用这个公式
                    intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minCityDistance1 + 24)) - 0.6;  //计算震中到城市点的最小距离（单位：米） minCityDistance1
                    intensity1 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minDistanceInKm1 + 24)) - 0.6; //计算雅安市行政边界最小距离（单位：米）minBoundaryDistance1
                    intensity2 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minCountyTownDistance1 + 24)) - 0.6; //正常更新最近的政府乡镇距离  minCountyTownDistan
                    intensity3 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(minVillageDistance + 24)) - 0.6;  //雅安市所有乡镇距离乡镇最小距离  minCountyTownDistance1

                }
                System.out.println("震中到城市点的最小距离的烈度: " + intensity);  //震中区最大地震烈度达%d度
                System.out.println("雅安市行政边界最小距离烈度(辖区外地震雅安最大烈度):  " + intensity1);
                System.out.println("最近的政府乡镇的烈度: " + intensity2);
                System.out.println("雅安市震中到乡镇点的最小距离: " + intensity3);

                // 四舍五入取整
                int roundedIntensity = (int) Math.round(intensity);
                int roundedIntensity1 = (int) Math.round(intensity1);
                int roundedIntensity2 = (int) Math.round(intensity2);
                int roundedIntensity3 = (int) Math.round(intensity3);

                // 打印变量的值
                System.out.println("取整后震中到城市点的最小距离的烈度: " + roundedIntensity);  //震中区最大地震烈度达%d度
                System.out.println("取整后雅安市行政边界最小距离烈度(取整后辖区外地震雅安最大烈度:): " + roundedIntensity1);
                System.out.println("取整后最近的政府乡镇的烈度: " + roundedIntensity2);
                System.out.println("取整后雅雅安市震中到乡镇点的最小距离: " + roundedIntensity3);


                //得到我市最大烈度
                seismicCategoryBO.setMaxIntensity(roundedIntensity3);
                System.out.println("maxIntensity烈度选取：roundedIntensity3取整后雅安市所有乡镇距离乡镇最小距离: " + roundedIntensity3);

                //得到我市最大烈度
                seismicCategoryBO.setOutside(roundedIntensity1);
                System.out.println("outside烈度选取：roundedIntensity3取整后雅安市所有乡镇距离乡镇最小距离: " + roundedIntensity1);


                // 如果最大烈度大于等于 6 度，计算所有乡镇的震中距以及烈度
                if (seismicCategoryBO.getMaxIntensity() >= 6) {
                    // 查询雅安市所有乡镇（yaan_villages表）
                    villageList = yaVillagesMapper.selectList(null);

                    // 初始化统计变量
                    seismicCategoryBO.setBig(0);   // 7度及以上的县区个数
                    seismicCategoryBO.setMiddle(0); // 6度及以上的县区个数
                    seismicCategoryBO.setSmall(0);  // 5度及以上的县区个数

                    // 遍历所有乡镇，计算与上传经纬度的距离并计算烈度
                    for (YaVillages village : villageList) {
                        Geometry geom = village.getGeom();
                        if (geom != null && geom instanceof Point) {
                            // 获取乡镇的经纬度
                            Point villagePoint = (Point) geom;
                            double villageLat = villagePoint.getY(); // 纬度
                            double villageLon = villagePoint.getX(); // 经度

                            // 计算震中到乡镇点的距离（单位：米）
                            double distanceToVillage = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), villageLat, villageLon);

                            // 计算震中到乡镇的烈度
                            double allIntensity3;
                            if (dto.getMagnitude() > 5.5) {
                                allIntensity3 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(distanceToVillage + 24)) - 0.4;
                            } else {
                                allIntensity3 = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(distanceToVillage + 24)) - 0.6;
                            }

                            // 根据烈度筛选县区并计数
                            if (allIntensity3 >= 8) {
                                seismicCategoryBO.setBig(seismicCategoryBO.getBig() + 1);// 7度及以上的县区
                            } else if (allIntensity3 >= 7) {
                                seismicCategoryBO.setMiddle(seismicCategoryBO.getMiddle() + 1);// 6度及以上的县区
                            } else if (allIntensity3 >= 6) {
                                seismicCategoryBO.setSmall(seismicCategoryBO.getSmall() + 1);// 5度及以上的县区
                            }
                        }
                    }


                    // 输出筛选结果
                    System.out.println("1.外地地震雅安最大烈度8度及以上时，筛选7度及以上县个数: double big = " + seismicCategoryBO.getBig());
                    System.out.println("1.外地地震雅安最大烈度7度及以上时，筛选6度、7度县个数: double middle = " + seismicCategoryBO.getMiddle());
                    System.out.println("1.外地地震雅安最大烈度6度及以上时，筛选5度、6度县个数: double small = " + seismicCategoryBO.getSmall());
                }


            }

        }

        System.out.println("最大烈度为: maxIntensity = " + seismicCategoryBO.getMaxIntensity());

        seismicCategoryBO.setMaxIntensityWithUnit(seismicCategoryBO.getMaxIntensity() + "度");   //B20 有"度"字

        System.out.println("maxIntensityWithUnit将最大烈度加上-度-字 ：" + seismicCategoryBO.getMaxIntensityWithUnit());

        // 输出筛选结果
        System.out.println("外地地震雅安最大烈度8度及以上时，筛选7度及以上县个数: double big = " + seismicCategoryBO.getBig());
        System.out.println("外地地震雅安最大烈度7度及以上时，筛选6度、7度县个数: double middle = " + seismicCategoryBO.getMiddle());
        System.out.println("外地地震雅安最大烈度6度及以上时，筛选5度、6度县个数: double small = " + seismicCategoryBO.getSmall());

        //---------最终影响-------
        String influence = ""; // 初始化影响的字符串  E31

        // 判断震中点最大烈度并输出影响描述
        if (seismicCategoryBO.getMaxIntensity() == 0 || seismicCategoryBO.getMaxIntensity() == 1 || seismicCategoryBO.getMaxIntensity() == 2) {
            influence = "无影响";
        } else if (seismicCategoryBO.getMaxIntensity() == 3) {
            influence = "影响较小";
        } else if (seismicCategoryBO.getMaxIntensity() == 4 || seismicCategoryBO.getMaxIntensity() == 5) {
            influence = "有一定影响";
        } else if (seismicCategoryBO.getMaxIntensity() == 6) {
            influence = "影响较大";
        } else if (seismicCategoryBO.getMaxIntensity() == 7 || seismicCategoryBO.getMaxIntensity() == 8 || seismicCategoryBO.getMaxIntensity() == 9) {
            influence = "影响非常大";
        } else if (seismicCategoryBO.getMaxIntensity() == 10 || seismicCategoryBO.getMaxIntensity() == 11 || seismicCategoryBO.getMaxIntensity() == 12) {
            influence = "影响巨大，部分区域是毁灭性的";
        } else {
            influence = "无影响"; // 如果输入的值不在有效范围内
        }

        // 输出影响结果
        System.out.println("影响：" + influence);

        //-------灾害等级-------

        String disasterLevel = "";  //C31

        if ("没有该地人口密度信息".equals(populationDensityBO.getPopulationDensity())) {
            disasterLevel = "";
        } else {
            // 将字符串类型的populationDensity转换为int
            int populationDensityInt = Integer.parseInt(populationDensityBO.getPopulationDensity());  // 字符串转换为整数
            // 检查人口密度和震级，根据条件返回灾害等级
            if (populationDensityInt >= 200) {
                if (dto.getMagnitude() >= 3.5 && dto.getMagnitude() < 4.0) {
                    disasterLevel = "属有感地震。";
                } else if (dto.getMagnitude() >= 4.0 && dto.getMagnitude() < 5.0) {
                    disasterLevel = "按照地震灾害事件分级，属一般地震灾害。";
                } else if (dto.getMagnitude() >= 5.0 && dto.getMagnitude() < 6.0) {
                    disasterLevel = "按照地震灾害事件分级，属较大地震灾害。";
                } else if (dto.getMagnitude() >= 6.0 && dto.getMagnitude() < 7.0) {
                    disasterLevel = "按照地震灾害事件分级，属重大地震灾害。";
                } else if (dto.getMagnitude() >= 7.0) {
                    disasterLevel = "按照地震灾害事件分级，属特别重大地震灾害。";
                } else {
                    disasterLevel = "";
                }
            } else {
                if (dto.getMagnitude() >= 3.5 && dto.getMagnitude() < 4.5) {
                    disasterLevel = "属有感地震。";
                } else if (dto.getMagnitude() >= 4.5 && dto.getMagnitude() < 5.5) {
                    disasterLevel = "按照地震灾害事件分级，属一般地震灾害。";
                } else if (dto.getMagnitude() >= 5.5 && dto.getMagnitude() < 6.5) {
                    disasterLevel = "按照地震灾害事件分级，属较大地震灾害。";
                } else if (dto.getMagnitude() >= 6.5 && dto.getMagnitude() < 7.5) {
                    disasterLevel = "按照地震灾害事件分级，属重大地震灾害。";
                } else if (dto.getMagnitude() >= 7.5) {
                    disasterLevel = "按照地震灾害事件分级，属特别重大地震灾害。";
                } else {
                    disasterLevel = "";
                }
            }
        }


        System.out.println("灾害等级:" + disasterLevel);

        seismicCategoryBO.setPanduan(String.format("本次地震是%s，%s综合判断：本次地震对我市%s。",
                seismicCategoryBO.getCategory(), disasterLevel, influence
        ));
        System.out.println(seismicCategoryBO.getPanduan());

        return seismicCategoryBO;
    }

    private void getAfterSeismicLoss(String eqId) {

        List<AssessmentResult> loss = assessmentResultMapper.selectList(null);

        // 初始化累加变量
        int totalDeath = 0; // 总死亡人数
        int injury = 0; //受伤人数
        BigDecimal totalEconomicLoss = BigDecimal.ZERO; // 总经济损失（万元）
        BigDecimal totalBuildingDamage = BigDecimal.ZERO; // 总建筑破坏面积（万平方米）

        System.out.println("event的值为：" + eqId);

        // 遍历匹配 eqid 并累加
        for (AssessmentResult material : loss) {
            if (eqId != null && eqId.equals(material.getEqId())) {
                // 打印匹配到的记录
                System.out.println("匹配到的数据: " + material);

                totalDeath += Optional.ofNullable(material.getDeath()).orElse(0);
                injury += Optional.ofNullable(material.getInjury()).orElse(0);

                totalEconomicLoss = totalEconomicLoss.add(
                        Optional.ofNullable(material.getEconomicLoss())
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(BigDecimal::new)
                                .orElse(BigDecimal.ZERO)
                );

                totalBuildingDamage = totalBuildingDamage.add(
                        Optional.ofNullable(material.getBuildingDamage())
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(BigDecimal::new)
                                .orElse(BigDecimal.ZERO)
                );
            }
        }


        // 输出结果
        System.out.println("****总死亡人数：" + totalDeath + " 人****");
        System.out.println("****总受伤人数：" + injury + " 人****");
        System.out.println("****总经济损失：" + totalEconomicLoss + " 万元****");
        BigDecimal totalEconomicLossTwo = totalEconomicLoss.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
        System.out.println("****总经济损失：" + totalEconomicLossTwo + " 亿元****");
        System.out.println("****总建筑破坏面积：" + totalBuildingDamage + " 万平方米****");
        // 计算总建筑破坏面积（平方公里）
        BigDecimal totalBuildingDamageTwo = totalBuildingDamage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        System.out.println("****总建筑破坏面积：" + totalBuildingDamageTwo + " 平方公里****");

        // 生成结果字符串
        String fuJinTownResult = PopulationEconomicImpactAnalyzer.generateDisasterImpactEstimate(totalDeath, injury, totalEconomicLossTwo.doubleValue(), totalBuildingDamageTwo.doubleValue());

        System.out.println(fuJinTownResult);

    }

    private PopulationDensityBO populationDensityFunction(AssessmentDTO dto, String aftershockConclusion) throws ParseException {

        PopulationDensityBO populationDensityBO = new PopulationDensityBO();

        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dto.getEqTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 2. 使用 Calendar 获取 年、月、日
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse);

        int year = calendar.get(Calendar.YEAR);   //B3
        int month = calendar.get(Calendar.MONTH) + 1;     //C3    // Calendar 的月份从 0 开始，所以 +1
        int day = calendar.get(Calendar.DAY_OF_MONTH);  //D3
        String monthDay = month + " • " + day; // C3•D3

        populationDensityBO.setYear(year);
        populationDensityBO.setMonth(month);
        populationDensityBO.setDay(day);
        populationDensityBO.setMonthDay(monthDay);

        // --------遍历查询雅安市人口密度----------------
        List<YaResidentPopulationDensity> density = yaResidentPopulationDensityMapper.selectList(null);

        // 用于存储匹配到的人口密度
        populationDensityBO.setPopulationDensity("没有该地人口密度信息");
        populationDensityBO.setCountyOrDistrict("县/区");
        populationDensityBO.setCityOrState("市/州");

        // 遍历查询匹配的 cityAndCounty 字段
        for (YaResidentPopulationDensity populationData : density) {
            if (dto.getEqName() != null && dto.getEqName().contains(populationData.getCityAndCounty())) {
                populationDensityBO.setPopulationDensity(populationData.getPopulationDensity());
                populationDensityBO.setCountyOrDistrict(populationData.getCountyOrDistrict());
                populationDensityBO.setCityOrState(populationData.getCityOrState());
                break; // 找到匹配项后，停止遍历
            }
        }


        System.out.println("人口密度：" + populationDensityBO.getPopulationDensity());   //  G26
        System.out.println("县/区：" + populationDensityBO.getCountyOrDistrict());  //  县/区  输出：泸定县  L3
        System.out.println("市/州：" + populationDensityBO.getCityOrState());  //  市/州 输出：甘孜州  K3


        populationDensityBO.setNewCountyOrDistrict(" ");
        populationDensityBO.setNewCountyOrDistrict(populationDensityBO.getCountyOrDistrict().replace("区", "").replace("县", ""));
        System.out.println("去除县/区后的县/区字段：" + populationDensityBO.getNewCountyOrDistrict());  // 输出：泸定

        //--------地区-------
        // 地区
        String position;
        if (dto.getEqName().contains("雅安")) { // 如果 eqName 包含“雅安”
            if (dto.getLatitude() <= 29.57) {
                position = "雅安南部";
            } else {
                position = "雅安中北部";
            }
        } else if (dto.getEqName().contains("甘孜")) { // 如果 eqName 包含“甘孜”
            if (dto.getLatitude() <= 31) {
                position = "甘孜中南部";
            } else {
                position = "甘孜北部";
            }
        } else {
            // 默认返回 eqName 本身
            position = dto.getEqName();
        }
        System.out.println("地区：" + position);


        String geography;


        if (position.contains("阿坝") || position.contains("甘孜北部") || position.contains("雅安中北部")) {
            geography = "大部分属于川西北丘状高原山地区";
        } else if (position.contains("甘孜中南部")) {
            geography = "大部分属于川西高山高原区";
        } else if (position.contains("凉山") || position.contains("攀枝花") || position.contains("乐山") || position.contains("雅安南部")) {
            geography = "大部分属于川西南中高山地区";
        } else if (position.contains("成都") || position.contains("德阳") || position.contains("眉山")) {
            geography = "大部分属于成都平原区";
        } else if (position.contains("广元") || position.contains("绵阳") || position.contains("南充") || position.contains("遂宁") ||
                position.contains("资阳") || position.contains("内江") || position.contains("自贡") ||
                position.contains("宜宾") || position.contains("泸州")) {
            geography = "大部分属于盆中丘陵区";
        } else if (position.contains("巴中")) {
            geography = "大部分属于米仓山大巴山中山区";
        } else if (position.contains("达州") || position.contains("广安")) {
            geography = "大部分属于盆东平行岭谷区";
        } else {
            geography = "不在判断区范围";
        }


        System.out.println("地理：" + geography);


        // 查询雅安市所有乡镇
        List<YaVillages> villageList0 = yaVillagesMapper.selectList(null); //????不太确定YaanVillages表名，数据库没开

        // 用 Map 存储所有乡镇名称及其对应的震中距
        Map<String, Double> villageDistances0 = new HashMap<>();

        // 遍历所有乡镇，计算与上传经纬度的距离
        for (YaVillages village : villageList0) {
            Geometry geom = village.getGeom();
            if (geom != null && geom instanceof Point) {
                // 获取乡镇的经纬度
                Point villagePoint = (Point) geom;
                double villageLat = villagePoint.getY(); // 纬度
                double villageLon = villagePoint.getX(); // 经度

                // 计算震中到乡镇点的距离（单位：米）
                double distanceToVillage = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), villageLat, villageLon);

                // 存储乡镇名称及其震中距
                villageDistances0.put(village.getVillagesName(), distanceToVillage);
            }
        }

        // **对乡镇按照震中距从小到大排序，并取前 8 个**
        List<Map.Entry<String, Double>> sortedVillages0 = new ArrayList<>(villageDistances0.entrySet());
        sortedVillages0.sort(Map.Entry.comparingByValue()); // 按震中距升序排序

        // 取前 8 个乡镇
        int limit0 = Math.min(8, sortedVillages0.size()); // 防止乡镇数量不足 8 个
        List<Map.Entry<String, Double>> topVillages = sortedVillages0.subList(0, limit0);

        // **输出前 8 个乡镇及其震中距**
        System.out.println("最近的 8 个乡镇及其震中距：");
        for (Map.Entry<String, Double> entry : topVillages) {
            System.out.println("乡镇名称: " + entry.getKey() + "，震中距: " + entry.getValue() + " 米");
        }


        // 存储乡镇名称、震中距和计算后的烈度
        Map<String, Double> intensities0 = new LinkedHashMap<>();

        // 遍历计算每个乡镇的烈度
        for (Map.Entry<String, Double> entry : villageDistances0.entrySet()) {
            String townName = entry.getKey();  // 乡镇名称
            double distance = entry.getValue(); // 震中距（D）

            // 计算 X 的值
            double X = (dto.getMagnitude() > 5.5) ? 0.4 : 0.6;

            // 计算烈度 I
            double intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(distance + 24)) - X;

            // 确保烈度不小于 0
            intensity = Math.max(intensity, 0);

            // 将烈度值四舍五入为整数
            intensity = Math.round(intensity);

            // 存入 Map
            intensities0.put(townName, intensity);
        }

        // **输出每个乡镇的烈度**
        for (Map.Entry<String, Double> entry : intensities0.entrySet()) {
            System.out.println("乡镇名称: " + entry.getKey() + "，震中距: " + villageDistances0.get(entry.getKey()) + " km，烈度: " + String.format("%.2f", entry.getValue()));
        }

        // **将 Map 转换成 List 并排序**
        List<Map.Entry<String, Double>> sortedList0 = new ArrayList<>(intensities0.entrySet());
        sortedList0.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())); // 按烈度降序排序

        // **输出排序后的乡镇烈度信息**
        System.out.println("乡镇名称 | 烈度");
        System.out.println("--------------------");
        for (Map.Entry<String, Double> entry : sortedList0) {
            System.out.printf("%s | %.2f%n", entry.getKey(), entry.getValue());
        }


        //---------------涉及我市行政村（社区）xxx万户，常住人口约xx万人。------------------------------------------


        List<YaVillageCommunity> quantity = yaVillageCommunityMapper.selectList(null);


        Map<String, Double> villageAndCommunityDistances = new LinkedHashMap<>();

        // 获取所有村（社区）的户数（风普）和常住人口（风普），并一一对应存储
        List<Map<String, Object>> data = new ArrayList<>();
        for (YaVillageCommunity villageAndCommunity : quantity) {


            Geometry geom = villageAndCommunity.getGeom();
            if (geom != null && geom instanceof Point) {
                // 获取政府县区的经纬度
                Point villageAndCommunityPoint = (Point) geom;
                double villageAndCommunityLat = villageAndCommunityPoint.getY(); // 纬度
                double villageAndCommunityLon = villageAndCommunityPoint.getX(); // 经度

                // 计算震中到县区点的距离（单位：米）
                double distanceToCountyTown = EarthquakeDistanceCalculator.calculateDistance(dto.getLatitude(), dto.getLongitude(), villageAndCommunityLat, villageAndCommunityLon);

                // 存储县区名称及其震中距
                villageAndCommunityDistances.put(villageAndCommunity.getVillageOrCommunity(), distanceToCountyTown);
            }


            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("villageOrCommunity", villageAndCommunity.getVillageOrCommunity()); // 村（社区）名称
            dataMap.put("numberOfHouseholdsOrFengpu", villageAndCommunity.getNumberOfHouseholdsOrFengpu()); // 户数
            dataMap.put("residentPopulationFengpu", villageAndCommunity.getResidentPopulationFengpu()); // 常住人口
            data.add(dataMap);
        }

        System.out.println("打印所有村（社区）的户数（风普）和常住人口（风普）前7个数据:");
        // 打印前7个数据（如果数据不足7个，则打印全部）
        int seven = Math.min(7, data.size()); // 确保不会超出索引范围
        for (int i = 0; i < seven; i++) {
            System.out.println("第" + i + "个：" + data.get(i));
        }

        // 存储乡镇名称、震中距和计算后的烈度
        Map<String, Double> all = new LinkedHashMap<>();

        // 遍历计算每个乡镇的烈度
        for (Map.Entry<String, Double> entry : villageAndCommunityDistances.entrySet()) {
            String townName = entry.getKey();  // 乡镇名称
            double distance = entry.getValue(); // 震中距（D）

            // 计算 X 的值
            double X = (dto.getMagnitude() > 5.5) ? 0.4 : 0.6;

            // 计算烈度 I
            double intensity = 7.3568 + 1.278 * dto.getMagnitude() - (5.0655 * Math.log10(distance + 24)) - X;

            // 确保烈度不小于 0
            intensity = Math.max(intensity, 0);

            // 将烈度值四舍五入为整数
            intensity = Math.round(intensity);

            // 存入 Map
            all.put(townName, intensity);
        }


        System.out.println("打印所有村（社区）的户数（风普）和常住人口（风普）前7个数据的烈度:");
        List<String> keys = new ArrayList<>(all.keySet()); // 将 Map 的键转换为列表
        int sevens = Math.min(11, keys.size()); // 确保不会超出索引范围
        for (int i = 0; i < sevens; i++) {
            String key = keys.get(i); // 获取第 i 个键
            Double intensity = all.get(key); // 通过键获取烈度
            System.out.println("第" + (i + 1) + "个：" + key + " 的烈度为 " + intensity);
        }

        // 计算 6 度以上（包含 6 度）的总户数
        double totalHouseholdsAbove6 = data.stream()
                .filter(map -> {
                    // 获取当前乡镇的名称
                    String villageOrCommunity = (String) map.get("villageOrCommunity");

                    // 获取该乡镇的烈度
                    Double intensity = all.get(villageOrCommunity);

                    // 确保烈度不为空，并且大于等于 6
                    return intensity != null && intensity >= 6;
                })
                .mapToDouble(map -> {
                    Object value = map.get("numberOfHouseholdsOrFengpu");
                    if (value == null) return 0.0;
                    return (value instanceof Number) ? ((Number) value).doubleValue() : 0.0;
                })
                .sum();

        //使用 mapToDouble() 取出户数 numberOfHouseholdsOrFengpu。
        //使用 sum() 计算总户数。

        System.out.println("6 度以上的总户数：" + totalHouseholdsAbove6);  //L19


        // 格式化输出
        String formattedHouseholds;
        if (totalHouseholdsAbove6 > 10000) {
            formattedHouseholds = String.format("%.2f万户", totalHouseholdsAbove6 / 10000);
        } else {
            formattedHouseholds = (int) totalHouseholdsAbove6 + "户";
        }

        System.out.println("格式后 6 度以上的户数：" + formattedHouseholds);   //M19


        // 计算 6 度以上（包含 6 度）的总常住人口
        double totalPopulationAbove6 = data.stream()
                .filter(map -> {
                    // 获取当前乡镇名称
                    String villageOrCommunity = (String) map.get("villageOrCommunity");

                    // 获取该乡镇的烈度
                    Double intensity = all.get(villageOrCommunity);

                    // 确保烈度不为空，并且大于等于 6
                    return intensity != null && intensity >= 6;
                })
                .mapToDouble(map -> {
                    Object value = map.get("residentPopulationFengpu");
                    if (value == null) return 0.0;
                    return (value instanceof Number) ? ((Number) value).doubleValue() : 0.0;
                })
                .sum();

        System.out.println("6 度以上的总常住人口：" + totalPopulationAbove6);  //N19


        // 格式化输出
        String formattedPopulation;
        if (totalPopulationAbove6 > 10000) {
            formattedPopulation = String.format("%.2f万人", totalPopulationAbove6 / 10000);
        } else {
            formattedPopulation = (int) totalPopulationAbove6 + "人";
        }

        System.out.println("格式后 6 度以上的常住人口：" + formattedPopulation);  //O19


        // 计算 6 度以上（包含 6 度）的村（社区）数量
        long numberOfCommunityAbove6 = data.stream()
                .filter(map -> {
                    // 获取当前村（社区）的名称
                    String villageOrCommunity = (String) map.get("villageOrCommunity");

                    // 获取该村（社区）的烈度
                    Double intensity = all.get(villageOrCommunity);

                    // 确保烈度不为空，并且大于等于 6
                    return intensity != null && intensity >= 6;
                })
                .count();

        System.out.println("6 度以上的村（社区）数量：" + numberOfCommunityAbove6);   //K19


        // 生成 communityStatement
        String communityStatement;
        if (numberOfCommunityAbove6 == 0) {
            communityStatement = "";
        } else {
            communityStatement = String.format(
                    "我市地震烈度6度及以上范围内，共涉及我市%d个行政村（社区）%s，常住人口约%s。",
                    numberOfCommunityAbove6, formattedHouseholds, formattedPopulation
            );
        }

        // 输出结果
        System.out.println(communityStatement);


        //--------- 构建基础描述----------


        // String result;

        // 先判断震中是否在四川
        if (!dto.getEqAddr().contains("四川")) {
            populationDensityBO.setResult("震中不在四川境内，无法判断。");
        } else {
            // 先判断人口密度
            if (!"没有该地人口密度信息".equals(populationDensityBO.getPopulationDensity())) {
                // 有人口密度信息
                if (!"不在判断区范围".equals(geography)) {
                    // 有地理范围信息，完整输出
                    populationDensityBO.setResult(String.format(
                            "地震%s，震区%s。震中%s人口密度约%s人/平方公里。",
                            aftershockConclusion, geography, populationDensityBO.getCountyOrDistrict(), populationDensityBO.getPopulationDensity()
                    ));
                } else {
                    // 没有地理范围信息，只输出人口密度部分
                    populationDensityBO.setResult(String.format("地震%s。震中%s人口密度约%s人/平方公里。",
                            aftershockConclusion, populationDensityBO.getCountyOrDistrict(), populationDensityBO.getPopulationDensity()
                    ));
                }
            } else {
                // 没有人口密度信息
                if (!"不在判断区范围".equals(geography)) {
                    // 有地理范围信息，去掉人口密度描述
                    populationDensityBO.setResult(String.format(
                            "地震%s，震区大部分属于%s。",
                            aftershockConclusion, geography
                    ));
                } else {
                    // 没有地理范围信息，基础描述
                    populationDensityBO.setResult(String.format(
                            "地震%s。",
                            aftershockConclusion
                    ));
                }
            }

            // 在结果后面拼接 communityStatement
            populationDensityBO.setResult(populationDensityBO.getResult() + communityStatement);

        }


        System.out.println("第一段：" + populationDensityBO.getResult());

        return populationDensityBO;
    }

    private String calculateQXValue(double longitude, double latitude) {

        // 四川省历史地震区县
        String lushan, wenchuan, jiuzhaigou, changning, luding; // 假设数据来自外部输入
        // 是否余震定论
        String aftershockConclusion = "";

        // Lushan 地区
        double QA_Lushan = EarthquakeDistanceCalculator.calculateQX(30.521, 102.913, latitude, longitude);
        double QB_Lushan = EarthquakeDistanceCalculator.calculateQX(30.444, 103.192, latitude, longitude);
        double QC_Lushan = EarthquakeDistanceCalculator.calculateQX(30.054, 102.998, latitude, longitude);
        double QD_Lushan = EarthquakeDistanceCalculator.calculateQX(30.162, 102.695, latitude, longitude);

        // Wenchuan 地区
        double QA_Wenchuan = EarthquakeDistanceCalculator.calculateQX(33.113, 105.338, latitude, longitude);
        double QB_Wenchuan = EarthquakeDistanceCalculator.calculateQX(32.678, 106.011, latitude, longitude);
        double QC_Wenchuan = EarthquakeDistanceCalculator.calculateQX(30.543, 103.560, latitude, longitude);
        double QD_Wenchuan = EarthquakeDistanceCalculator.calculateQX(31.053, 102.779, latitude, longitude);

        // Jiuzhaigou 地区
        double QA_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQX(33.468, 103.591, latitude, longitude);
        double QB_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQX(33.53, 103.858, latitude, longitude);
        double QC_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQX(32.984, 104.106, latitude, longitude);
        double QD_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQX(32.893, 103.875, latitude, longitude);

        // Changning 地区
        double QA_Changning = EarthquakeDistanceCalculator.calculateQX(28.4445, 104.6478, latitude, longitude);
        double QB_Changning = EarthquakeDistanceCalculator.calculateQX(28.5644, 104.7633, latitude, longitude);
        double QC_Changning = EarthquakeDistanceCalculator.calculateQX(28.3907, 105.0221, latitude, longitude);
        double QD_Changning = EarthquakeDistanceCalculator.calculateQX(28.2705, 104.9237, latitude, longitude);

        // Luding 地区
        double QA_Luding = EarthquakeDistanceCalculator.calculateQX(29.7232, 101.9167, latitude, longitude);
        double QB_Luding = EarthquakeDistanceCalculator.calculateQX(29.7417, 102.1447, latitude, longitude);
        double QC_Luding = EarthquakeDistanceCalculator.calculateQX(29.3869, 102.3095, latitude, longitude);
        double QD_Luding = EarthquakeDistanceCalculator.calculateQX(29.3032, 102.0973, latitude, longitude);


        log.info("Lushan QA:{} " + QA_Lushan + ", QB:{} " + QB_Lushan + ", QC: {}" + QC_Lushan + ", QD:{} " + QD_Lushan);
        log.info("Wenchuan QA:{} " + QA_Wenchuan + ", QB: {}" + QB_Wenchuan + ", QC:{} " + QC_Wenchuan + ", QD: {}" + QD_Wenchuan);
        log.info("Jiuzhaigou QA:{} " + QA_Jiuzhaigou + ", QB:{} " + QB_Jiuzhaigou + ", QC: {}" + QC_Jiuzhaigou + ", QD: {}" + QD_Jiuzhaigou);
        log.info("Changning QA: {}" + QA_Changning + ", QB: {}" + QB_Changning + ", QC: {}" + QC_Changning + ", QD:{} " + QD_Changning);
        log.info("Luding QA:{} " + QA_Luding + ", QB: {}" + QB_Luding + ", QC: {}" + QC_Luding + ", QD: {}" + QD_Luding);


        // 计算各地区的 QAB, QBC, QCD, QAD
        double QAB_Lushan = EarthquakeDistanceCalculator.calculateQAB(QA_Lushan, QB_Lushan, 28.0730);
        double QBC_Lushan = EarthquakeDistanceCalculator.calculateQBC(QB_Lushan, QC_Lushan, 47.2002);
        double QCD_Lushan = EarthquakeDistanceCalculator.calculateQCD(QC_Lushan, QD_Lushan, 31.5235);
        double QAD_Lushan = EarthquakeDistanceCalculator.calculateQDA(QA_Lushan, QD_Lushan, 45.0686);

        double QAB_Wenchuan = EarthquakeDistanceCalculator.calculateQAB(QA_Wenchuan, QB_Wenchuan, 79.2962);
        double QBC_Wenchuan = EarthquakeDistanceCalculator.calculateQBC(QB_Wenchuan, QC_Wenchuan, 331.9871);
        double QCD_Wenchuan = EarthquakeDistanceCalculator.calculateQCD(QC_Wenchuan, QD_Wenchuan, 93.7043);
        double QAD_Wenchuan = EarthquakeDistanceCalculator.calculateQDA(QA_Wenchuan, QD_Wenchuan, 332.5328);

        double QAB_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQAB(QA_Jiuzhaigou, QB_Jiuzhaigou, 25.6995);
        double QBC_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQBC(QB_Jiuzhaigou, QC_Jiuzhaigou, 64.9442);
        double QCD_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQCD(QC_Jiuzhaigou, QD_Jiuzhaigou, 23.8138);
        double QAD_Jiuzhaigou = EarthquakeDistanceCalculator.calculateQDA(QA_Jiuzhaigou, QD_Jiuzhaigou, 69.1846);

        double QAB_Changning = EarthquakeDistanceCalculator.calculateQAB(QA_Changning, QB_Changning, 17.4679);
        double QBC_Changning = EarthquakeDistanceCalculator.calculateQBC(QB_Changning, QC_Changning, 31.8262);
        double QCD_Changning = EarthquakeDistanceCalculator.calculateQCD(QC_Changning, QD_Changning, 16.4741);
        double QAD_Changning = EarthquakeDistanceCalculator.calculateQDA(QA_Changning, QD_Changning, 33.2144);

        double QAB_Luding = EarthquakeDistanceCalculator.calculateQAB(QA_Luding, QB_Luding, 22.1107);
        double QBC_Luding = EarthquakeDistanceCalculator.calculateQBC(QB_Luding, QC_Luding, 42.5501);
        double QCD_Luding = EarthquakeDistanceCalculator.calculateQCD(QC_Luding, QD_Luding, 22.5756);
        double QAD_Luding = EarthquakeDistanceCalculator.calculateQDA(QA_Luding, QD_Luding, 49.8646);

        log.info("Lushan - QAB: " + QAB_Lushan + ", QBC: " + QBC_Lushan + ", QCD: " + QCD_Lushan + ", QAD: " + QAD_Lushan);
        log.info("Wenchuan - QAB: " + QAB_Wenchuan + ", QBC: " + QBC_Wenchuan + ", QCD: " + QCD_Wenchuan + ", QAD: " + QAD_Wenchuan);
        log.info("Jiuzhaigou - QAB: " + QAB_Jiuzhaigou + ", QBC: " + QBC_Jiuzhaigou + ", QCD: " + QCD_Jiuzhaigou + ", QAD: " + QAD_Jiuzhaigou);
        log.info("Changning - QAB: " + QAB_Changning + ", QBC: " + QBC_Changning + ", QCD: " + QCD_Changning + ", QAD: " + QAD_Changning);
        log.info("Luding - QAB: " + QAB_Luding + ", QBC: " + QBC_Luding + ", QCD: " + QCD_Luding + ", QAD: " + QAD_Luding);


        // 调用函数计算各地区的区域面积并打印
        double lushanArea = EarthquakeDistanceCalculator.calculateArea(QAB_Lushan, QBC_Lushan, QCD_Lushan, QAD_Lushan, QA_Lushan, QB_Lushan, QC_Lushan, QD_Lushan, 28.0730, 47.2002, 31.5235, 45.0686);
        log.info("Lushan Area 计算结果: " + lushanArea);

        double wenchuanArea = EarthquakeDistanceCalculator.calculateArea(QAB_Wenchuan, QBC_Wenchuan, QCD_Wenchuan, QAD_Wenchuan, QA_Wenchuan, QB_Wenchuan, QC_Wenchuan, QD_Wenchuan, 79.2962, 331.9871, 93.7043, 332.5328);
        log.info("Wenchuan Area 计算结果: " + wenchuanArea);

        double jiuzhaigouArea = EarthquakeDistanceCalculator.calculateArea(QAB_Jiuzhaigou, QBC_Jiuzhaigou, QCD_Jiuzhaigou, QAD_Jiuzhaigou, QA_Jiuzhaigou, QB_Jiuzhaigou, QC_Jiuzhaigou, QD_Jiuzhaigou, 25.6995, 64.9442, 23.8138, 69.1846);
        log.info("Jiuzhaigou Area 计算结果: " + jiuzhaigouArea);

        double changningArea = EarthquakeDistanceCalculator.calculateArea(QAB_Changning, QBC_Changning, QCD_Changning, QAD_Changning, QA_Changning, QB_Changning, QC_Changning, QD_Changning, 17.4679, 31.8262, 16.4741, 33.2144);
        log.info("Changning Area 计算结果: " + changningArea);

        double ludingArea = EarthquakeDistanceCalculator.calculateArea(QAB_Luding, QBC_Luding, QCD_Luding, QAD_Luding, QA_Luding, QB_Luding, QC_Luding, QD_Luding, 22.1107, 42.5501, 22.5756, 49.8646);
        log.info("Luding Area 计算结果: " + ludingArea);

        // 判断该地震是否属于余震范围内
        if (lushanArea <= 1370) {
            lushan = "是";
        } else {
            lushan = "否";
        }
        if (wenchuanArea <= 28460) {
            wenchuan = "是";
        } else {
            wenchuan = "否";
        }
        if (jiuzhaigouArea <= 1654) {
            jiuzhaigou = "是";
        } else {
            jiuzhaigou = "否";
        }
        if (changningArea <= 552) {
            changning = "是";
        } else {
            changning = "否";
        }
        if (ludingArea <= 1015) {
            luding = "是";
        } else {
            luding = "否";
        }

        // 根据四川省历史地震下定论余震范围
        if ("是".equals(lushan)) {
            aftershockConclusion = "所在位置位于2013年“4·20”芦山7.0级地震余震活动区范围";
        } else if ("是".equals(wenchuan)) {
            aftershockConclusion = "所在位置位于2008年“5·12”汶川8.0级地震余震活动区范围";
        } else if ("是".equals(jiuzhaigou)) {
            aftershockConclusion = "所在位置位于2017年“8·8”九寨沟7.0级地震余震活动区范围";
        } else if ("是".equals(changning)) {
            aftershockConclusion = "所在位置位于2019年“6·17”长宁6.0级地震余震活动区范围";
        } else if ("是".equals(luding)) {
            aftershockConclusion = "所在位置位于2022年“9·5”泸定6.8级地震余震活动区范围";
        } else {
            aftershockConclusion = "不在近年有影响的地震余震活动区范围";
        }

        log.info("余震结论判断：" + aftershockConclusion);

        // 返回是否余震信息
        return aftershockConclusion;
    }

}
