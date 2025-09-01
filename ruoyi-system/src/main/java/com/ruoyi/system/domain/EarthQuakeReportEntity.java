package com.ruoyi.system.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 暴雨分析报告实体类
 */
@Data
public class EarthQuakeReportEntity {
    /*
     * 表头时间
     */
    private String reportTime;                              // 报告时间

    /*
     * 地震概况部分
     */
    private String earthQuakeTime;//地震时间
    private String earthQuakePosition;//地震位置
    private String earthQuakeLon;//震源经度
    private String earthQuakeLat;//震源纬度
    private String earthQuakeMagnitude;//震级
    private String earthQuakeSourceDepth;//震源深度

    /*
     * 风险评估部分
     */
    private String earthQuakeCountry;//所在地区乡/街道
    private String earthQuakePopulationDensity;//所在地区乡/街道人口密度
    private String earthQuakeIntensity;//重灾区烈度
    private String earthQuakeDisasterArea;//重灾区面积(km2)
    private String earthQuakeSumGDP;//灾区GDP(亿元)
    private String earthQuakeInfluencePopulation;//地震影响人口
    private String earthQuakeDeath;//地震预计伤亡人数
    private String earthQuakeFaultZone;//震中最近断裂带
    private List<Hospital> earthQuakeHospital;//震中附近50km内的医院列表(名称/总床位)
    private Object earthQuakeInfluenceGraph;//地震影响估计范围分布图（未知类型所以用object
    private Object earthQuakeFaultZoneGraph;//地震震中附近断裂带图
    private Object earthQuakeHospitalGraph;//地震震中附近医院分布图

    @Data
    public class Hospital {
        private String hospitalName;
        private String hospitalBeds;
        private String hospitalAddress;
        private String hospitalLevel;
    }

    /*
    应急处置建议
     */
    private String earthQuakeEmergencyLevel;//地震响应等级

    /**
     * 构造器初始化数据
     */
    public EarthQuakeReportEntity() {
        this.earthQuakeHospital = new ArrayList<>();
    }
}