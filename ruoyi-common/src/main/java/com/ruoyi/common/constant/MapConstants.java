package com.ruoyi.common.constant;

/**
 * @author: xiaodemos
 * @date: 2025-04-03 10:10
 * @description: 地图常量类
 */


public class MapConstants {


    public static final String[] INTENSITY_LEVEL = new String[] {  // 烈度罗马数值
            "Ⅰ度", "Ⅱ度", "Ⅲ度", "Ⅳ度", "Ⅴ度", "Ⅵ度", "Ⅶ度", "Ⅷ度", "Ⅸ度", "Ⅹ度", "Ⅺ度", "Ⅻ度"
    };
    public static final Integer[] INTENSITY_LEVEL_FIGURE = new Integer[] { // 烈度罗马阿拉伯数值
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
    };

    public static final Double SEISMIC_6_GRADE = 6.0;

    public static final String SEISMIC_POINT = "震中";
    public static final String SEISMIC_INTENSITY = "烈度圈";
    public static final String SEISMIC_INTENSITY_AFFECTED_AREA = "影响场";
    public static final String SEISMIC_INTENSITY_TEXT = "烈度文本";

    public static final String GRADE = "级";

    public static final String SEISMIC_DISTRIBUTION = "影响估计范围分布图";
    public static final String SEISMIC_POTENT = "震区地震动峰值加速度区划图";
    public static final String SEISMIC_TRAFFIC = "震区交通图";
    public static final String SEISMIC_HISTORY_DISTRIBUTION = "震区历史地震分布图";
    public static final String SEISMIC_POTENTIAL_GEOPHYSICAL_DISTRIBUTION_LEVEL = "震区烈度区划图";
    public static final String SEISMIC_POTENTIAL_GEOPHYSICAL_DISTRIBUTION = "震区潜在地质灾害分布图";
    public static final String SEISMIC_DANGER_SOURCE = "震区危险源分布图";
    public static final String SEISMIC_RUPTURE = "震中附近活动断裂图";
    public static final String SEISMIC_RESCUE_TEAM = "震中附近救援队伍分布图";
    public static final String SEISMIC_TOURIST_SPOT = "震中附近旅游景点分布图";
    public static final String SEISMIC_SAFE_PLACE = "震中附近疏散场地分布图";
    public static final String SEISMIC_RESERVOIR = "震中附近水库分布图";
    public static final String SEISMIC_SCHOOL = "震中附近学校分布图";
    public static final String SEISMIC_HOSPITAL = "震中附近医院分布图";
    public static final String SEISMIC_IMPORTANT_OBJECT = "震中附近重要目标分布图";
    // public static final String SEISMIC_POSITION_LAND = "震中位置分布图（地形图底图）";
    public static final String SEISMIC_POSITION_ADMINISTRATIVE = "震中位置分布图（行政区划底图）";

    public static final String[] SEISMIC_MAPS = {
            // SEISMIC_DISTRIBUTION,
            SEISMIC_TRAFFIC,    // 交通
            SEISMIC_SAFE_PLACE, // 疏散场地
            SEISMIC_RESCUE_TEAM,    // 救援队伍
            SEISMIC_POSITION_ADMINISTRATIVE, // 行政区划
            SEISMIC_HOSPITAL,   // 医院
            SEISMIC_SCHOOL,     // 学校
            SEISMIC_RESERVOIR, //水库
            SEISMIC_DANGER_SOURCE,  // 危险源
            SEISMIC_RUPTURE, // 活动断裂
            SEISMIC_POTENT, // 地震动峰值加速度
            SEISMIC_HISTORY_DISTRIBUTION, // 历史地震分布
            SEISMIC_POTENTIAL_GEOPHYSICAL_DISTRIBUTION_LEVEL, // 潜在地质灾害区划
            SEISMIC_POTENTIAL_GEOPHYSICAL_DISTRIBUTION, // 潜在地质灾害
            SEISMIC_TOURIST_SPOT, // 旅游
            SEISMIC_IMPORTANT_OBJECT, // 重要目标
            // SEISMIC_POSITION_LAND,
    };


}
