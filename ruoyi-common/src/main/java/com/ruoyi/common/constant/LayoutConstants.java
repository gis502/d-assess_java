package com.ruoyi.common.constant;

/**
 * @author: xiaodemos
 * @date: 2025-04-05 10:37
 * @description: 布局常量类
 */


public class LayoutConstants {

    public static final int DPI = 600;  // 出图分辨率
    public static final int COMPRESS = 100; // 压缩图片（0-100）100表示不压缩
    public static final String EXTENSION_TYPE = ".jpg";
    public static final String IMAGE_TYPE = "图片";    // 文件类型
    public static final String WORD_TYPE = "文档";    // 文件类型
    public static final String SIZE = "A3"; // 专题图尺寸
    public static final Integer THEMATIC_TYPE = 1;
    public static final Integer DOCUMENT_TYPE = 2;


    public static final String PICTURE_PREFIX= "E:/upload/专题图/";
    public static final String REPORTS_PREFIX= "E:/upload/灾情报告/";
    public static final String AFFECTED_PREFIX= "E:/upload/地震影响场/";

    public static final String OUTPUT_FILED = "图件下载失败";

    public static final String UNIT = "西安市应急管理局";


    public static final String SEISMIC_DISTRIBUTION = "影响震区估计范围分布图";
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
    public static final String SEISMIC_POSITION_ADMINISTRATIVE = "震中位置分布图（行政区划底图）";
    //public static final String SEISMIC_POSITION_LAND = "震中位置分布图（地形图底图）";

    public static final String[] SEISMIC_LAYOUTS = {
            SEISMIC_DISTRIBUTION,
            SEISMIC_POTENT,
            SEISMIC_TRAFFIC,
            SEISMIC_HISTORY_DISTRIBUTION,
            SEISMIC_POTENTIAL_GEOPHYSICAL_DISTRIBUTION_LEVEL,
            SEISMIC_POTENTIAL_GEOPHYSICAL_DISTRIBUTION,
            SEISMIC_DANGER_SOURCE,
            SEISMIC_RUPTURE,
            SEISMIC_RESCUE_TEAM,
            SEISMIC_TOURIST_SPOT,
            SEISMIC_SAFE_PLACE,
            SEISMIC_RESERVOIR,
            SEISMIC_SCHOOL,
            SEISMIC_HOSPITAL,
            SEISMIC_IMPORTANT_OBJECT,
            SEISMIC_POSITION_ADMINISTRATIVE
            // SEISMIC_POSITION_LAND,
    };

}
