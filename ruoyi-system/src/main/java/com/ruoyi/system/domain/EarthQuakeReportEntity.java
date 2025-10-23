package com.ruoyi.system.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 暴雨分析报告实体类
 */
@Data
public class EarthQuakeReportEntity {
    private String eqId;
    private String eqqueueId;
    /*
     * 表头时间
     */
    private LocalDateTime reportTime;                              // 报告时间

    /*
     * 地震概况部分
     */
    private LocalDateTime earthQuakeTime;//地震时间
    private String earthQuakePosition;//地震位置
    private double earthQuakeLon;//震源经度
    private double earthQuakeLat;//震源纬度
    private double earthQuakeMagnitude;//震级
    private double earthQuakeSourceDepth;//震源深度
    /*
     * 风险评估部分
     */
    private String earthQuakeIntensity;//重灾区烈度
    private String earthQuakeDisasterArea;//重灾区面积(km2)
    private String earthQuakeInfluencePopulationMax;//地震影响人口最大值
    private String earthQuakeInfluencePopulationMin;//地震影响人口最小值
    private String earthQuakeDeathMax;//地震预计伤亡人数最大值
    private String earthQuakeDeathMin;//地震预计伤亡人数最小值
    private String earthQuakeFaultZone;//震中最近断裂带
    private List<Hospital> earthQuakeHospital;//震中附近5km内的医院列表(名称/总床位)
    private List<FireFighter> earthQuakeFireFighter;//震中附近100km内的消防队信息(名称/人数)
    private List<StorePoint> earthQuakeStorePoint;// 震中附近100km内的救援物资信息
    private String earthQuakeInfluenceGraph;//地震影响估计范围分布图路径
    private String earthQuakeFaultZoneGraph;//地震震中附近断裂带图路径
    private String earthQuakeHospitalGraph;//地震震中附近医院分布图路径
    private String earthQuakeFireFighterGraph;// 地震震中附近消防队分布图路径
    private String earthQuakeStorePointGraph;// 地震震中附近救援物资分布图路径

    @Data
    public class Hospital {
        private String hospitalName;
        private String hospitalBeds;
        private String hospitalAddress;
        private String hospitalLevel;
    }

    @Data
    public class FireFighter {
        private String fireFighterName;
        private String fireFighterType;
        private String fireFighterAddress;
        private String fireFighterNum;
    }

    @Data
    public class StorePoint {
        private String storePointName;
        private String storePointAddress;
        private String storePointDep;
        private String storePointNum;
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
        this.earthQuakeFireFighter = new ArrayList<>();
        this.earthQuakeStorePoint = new ArrayList<>();
    }
}