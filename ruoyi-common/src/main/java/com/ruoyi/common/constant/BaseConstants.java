package com.ruoyi.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: xiaodemos
 * @date: 2025-04-05 11:33
 * @description: 基本常量类
 */


public class BaseConstants {
    public static final Double PROGRESS_ZERO = 0.0;     // 进度条初始化状态
    public static final Integer AUTO_TRIGGER = 0;   // 自动触发
    public static final Integer ARTI_TRIGGER = 1;   // 手动触发
    public static final String ASSESSMENT_STATE_NOTHING = "未开始";
    public static final String ASSESSMENT_STATE_COMPUTING = "正在计算中";
    public static final String ASSESSMENT_STATE_FINISH = "正常完成";
    public static final String ASSESSMENT_STATE_ARTIFICIAL_STOP = "人工停止";
    public static final String ASSESSMENT_STATE_ABNORMAL = "异常中断";
    public static final String ASSESSMENT_STATE_TIMEOUT = "超时结束";
    public static final Integer ASSESSMENT_INIT = 1;    // 评估批次初始化状态
    public static final String XI_AN_AREA_CODE = "610100";// 西安市行政区划代码
    public static final String FILE_CREATE_FILED = "文件夹创建失败";
    public static final String TRIGGER_FILED = "地震启动失败";
    public static final String TRIGGER_ERROR = "地震启动异常";
    public static final String TRIGGER_SUCCESS = "地震启动成功";
    public static final String REASSESSMENT_FILED = "地震重新评估失败";
    public static final String REASSESSMENT_ERROR = "地震重新评估异常";
    public static final String REASSESSMENT_SUCCESS = "地震重新评估成功";
    public static final String QUERY_PARAMS_ERROR = "地图输出失败";
    public static final String THEMATIC_MAP_ERROR = "专题图获取失败";
    public static final String BASE_INFO_ERROR = "获取数据失败";
    public static final String FULL_NAME_SUFFIX = "级地震";
    public static final String RAIN_NAME_PREFIX = "小时持续降雨";
    public static final String RAIN_NAME_SUFFIX = "mm";
    public static final String PARAMETER_ERROR = "参数异常";
    public static final String PARSE_ERROR = "解析失败";

    // TODO 需要改成Linux路径
    public static final String XI_AN_SEISMIC_WORKSPACE_PATH = "D:/makemap/supermap/工作空间/地震专题图.smwu";
    public static final String XI_AN_STORM_WORKSPACE_PATH = "D:/makemap/supermap/工作空间/暴雨专题图.smwu";
    public static final String XI_AN_MAP_DATASETS_NAME = "西安智慧应急多灾害链平台";
    public static final String XI_AN_SEISMIC_DATASETS_NAME = "西安项目地震数据源";   // 放置地震数据源
    public static final String XI_AN_STORM_DATASETS_NAME = "西安项目暴雨数据源";     // 放置暴雨数据源
    public static final String XI_AN_RUPTURE_NAME = "陕西省断层";

    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String FILE_NOT_FOUND_ERROR = "文件不存在";
    public static final int DPI = 300;  // 出图分辨率
    public static final int COMPRESS = 100; // 压缩图片（0-100）100表示不压缩
    public static final String EXTENSION_TYPE = ".jpg";     // 出图照片类型
    public static final String REPORTS_EXTENSION_TYPE = ".docx";     // 出图照片类型
    public static final String IMAGE_TYPE = "图片";    // 文件类型
    public static final String WORD_TYPE = "文档";    // 文件类型
    public static final String SIZE = "A3"; // 专题图尺寸
    public static final Integer THEMATIC_TYPE = 1;
    public static final Integer DOCUMENT_TYPE = 2;
    // TODO 需要改成Linux服务器路径
    public static final String PICTURE_PREFIX = "D:/项目/home/output/seismic-disaster/thematic/";
    public static final String REPORTS_PREFIX = "D:/项目/home/output/seismic-disaster/reports/";
    public static final String STORM_PICTURE_PREFIX = "D:/项目/home/output/storm-disaster/thematic/";
    public static final String STORM_REPORTS_PREFIX = "D:/项目/home/output/storm-disaster/reports/";
    public static final String HTTP_NGINX_PREFIX = "http://10.22.245.247:80";

    public static final String OUTPUT_FILED = "图件下载失败";
    public static final String UNIT = "西安市应急管理局";

    public static final Double SEISMIC_6_GRADE = 6.0;   // 六级地震
    public static final String SEISMIC_POINT = "震中";
    public static final String SEISMIC_INTENSITY = "烈度圈";
    public static final String SEISMIC_INTENSITY_AFFECTED_AREA = "影响场";
    public static final String SEISMIC_INTENSITY_TEXT = "烈度文本";
    public static final String GRADE = "级";

    // 西安地震专题图
    public static final String XIAN_SEISMIC_DISTRIBUTION = "影响估计范围分布图";
    public static final String XIAN_SEISMIC_TRAFFIC = "震区交通图";
    public static final String XIAN_SEISMIC_RUPTURE = "震区附近断层分布图";
    public static final String XIAN_SEISMIC_IMPORTANT_OBJECTS = "震区附近重要目标分布图";
    public static final String XIAN_SEISMIC_DANGER_SOURCE = "震区附近危险源分布图";
    public static final String XIAN_SEISMIC_RESCUE_TEAM = "震区附近救援队伍分布图";
    public static final String XIAN_SEISMIC_SAFE_PLACE = "震区避难场所分布图";
    public static final String XIAN_SEISMIC_PUBLIC_PLACE = "震区公共场所分布图";
    public static final String XIAN_SEISMIC_SCHOOL = "震区附近学校分布图";
    public static final String XIAN_SEISMIC_HOSPITAL = "震区附近医院分布图";
    public static final String XIAN_SEISMIC_RESCUE_MATERIAL = "震区附近救援物资分布图";
    public static final String XIAN_SEISMIC_TOURIST_SPOT = "震区附近旅游景点分布图";
    public static final String XIAN_SEISMIC_RESERVOIR = "震区附近水库分布图";
    public static final String XIAN_SEISMIC_HIDE_POINT = "震区地质灾害隐患点分布图";
    public static final String XIAN_SEISMIC_RISK_AREA = "震区地质灾害风险区分布图";
    public static final String XIAN_SEISMIC_PEOPLE = "震区附近人口密度分布图";
    public static final String XIAN_SEISMIC_CROPS = "震区附近农作物密度分布图";


    // 西安暴雨专题图名称
    public static final String XIAN_STORM_FLOOD_HIDE_PEOPLE = "暴雨山洪潜在隐患点及人口分布图";
    public static final String XIAN_STORM_FLOOD_HIDE_CROPS = "暴雨山洪潜在隐患点及农作物分布图";
    public static final String XIAN_STORM_WATER_ACCUMULATE_HIDE_PEOPLE = "暴雨内涝潜在隐患点及人口分布图";
    public static final String XIAN_STORM_WATER_ACCUMULATE_HIDE_CROPS = "暴雨内涝潜在隐患点及农作物分布图";
    public static final String XIAN_STORM_SLIDE_HIDE_PEOPLE = "暴雨滑坡潜在隐患点及人口分布图";
    public static final String XIAN_STORM_SLIDE_HIDE_CROPS = "暴雨滑坡潜在隐患点及农作物分布图";
    public static final String XIAN_STORM_FLOWS_HIDE_PEOPLE = "暴雨泥石流潜在隐患点及人口分布图";
    public static final String XIAN_STORM_FLOWS_HIDE_CROPS = "暴雨泥石流潜在隐患点及农作物分布图";
    public static final String XIAN_STORM_CITY_LIFE_LINE = "暴雨城市生命线工程分布图";
    public static final String XIAN_STORM_FLOOD_CONTROL = "暴雨防汛物资分布图";
    public static final String XIAN_STORM_RESERVOIR = "暴雨附近水库分布图";
    public static final String XIAN_STORM_RISK_AREA = "暴雨地质灾害风险区分布图";
    public static final String XIAN_STORM_SAFE_PLACE = "暴雨避难场所分布图";
    public static final String XIAN_STORM_HOSPITAL = "暴雨附近医院分布图";
    public static final String XIAN_STORM_RESCUE_TEAMS = "暴雨附近救援队伍分布图";

    // 西安地震专题图名称
    public static final String[] XIAN_SEISMIC_MAPS = {
            XIAN_SEISMIC_IMPORTANT_OBJECTS, // 重要目标
            XIAN_SEISMIC_RESERVOIR, // 水库
            XIAN_SEISMIC_TOURIST_SPOT,  // 旅游景点
            XIAN_SEISMIC_RUPTURE,   // 断裂带
            XIAN_SEISMIC_RESCUE_TEAM,   // 救援队伍
            XIAN_SEISMIC_RESCUE_MATERIAL,   // 救援物资
            XIAN_SEISMIC_SCHOOL,    // 学校
            XIAN_SEISMIC_DANGER_SOURCE, // 危险源
            XIAN_SEISMIC_HOSPITAL,   // 医院
            XIAN_SEISMIC_SAFE_PLACE,    // 避难场所
            XIAN_SEISMIC_RISK_AREA,  // 风险区域
            XIAN_SEISMIC_HIDE_POINT,    // 隐患点
            XIAN_SEISMIC_PUBLIC_PLACE,  // 公共场所
            XIAN_SEISMIC_TRAFFIC,   // 交通
            XIAN_SEISMIC_DISTRIBUTION, // 影响范围
            XIAN_SEISMIC_PEOPLE,   // 人口
            XIAN_SEISMIC_CROPS // 农作物
    };

    // 西安暴雨专题图名称
    public static final String[] XIAN_STORM_MAPS = {
            XIAN_STORM_RESERVOIR, // 暴雨附近水库
            XIAN_STORM_RESCUE_TEAMS, // 暴雨附近救援队伍
            XIAN_STORM_HOSPITAL, // 暴雨附近医院
            XIAN_STORM_FLOOD_CONTROL, // 暴雨防汛物资
            XIAN_STORM_SAFE_PLACE, // 暴雨避难场所
            XIAN_STORM_SLIDE_HIDE_CROPS, // 暴雨滑坡潜在隐患点及农作物
            XIAN_STORM_SLIDE_HIDE_PEOPLE, // 暴雨滑坡潜在隐患点及人口
            XIAN_STORM_FLOWS_HIDE_CROPS, // 暴雨泥石流潜在隐患点及农作物
            XIAN_STORM_FLOWS_HIDE_PEOPLE, // 暴雨泥石流潜在隐患点及人口
            XIAN_STORM_FLOOD_HIDE_CROPS, // 暴雨山洪农作物
            XIAN_STORM_FLOOD_HIDE_PEOPLE,   // 暴雨山洪人口
            XIAN_STORM_CITY_LIFE_LINE, // 暴雨城市生命线工程
            XIAN_STORM_RISK_AREA, // 暴雨地质灾害风险区
            XIAN_STORM_WATER_ACCUMULATE_HIDE_CROPS, // 暴雨内涝潜在隐患点及农作物
            XIAN_STORM_WATER_ACCUMULATE_HIDE_PEOPLE, // 暴雨内涝人口
    };


    // 设置专题图比例尺
    public static final Map<String, Double> MAP_SCALE = new HashMap<String, Double>() {{
        // 地震专题图比例尺
        put(XIAN_SEISMIC_DISTRIBUTION, 1 / 600000.0);  // 地震影响范围分布图 1:600000
        put(XIAN_SEISMIC_TRAFFIC, 1 / 350000.0);       // 地震交通分布图
        put(XIAN_SEISMIC_RUPTURE, 1 / 700000.0);       // 地震断层分布图
        put(XIAN_SEISMIC_IMPORTANT_OBJECTS, 1 / 350000.0); // 地震重要目标分布图
        put(XIAN_SEISMIC_DANGER_SOURCE, 1 / 350000.0); // 地震危险源分布图
        put(XIAN_SEISMIC_RESCUE_TEAM, 1 / 400000.0);   // 地震救援队伍分布图
        put(XIAN_SEISMIC_SAFE_PLACE, 1 / 250000.0); // 地震避难场所分布图
        put(XIAN_SEISMIC_PUBLIC_PLACE, 1 / 300000.0); // 地震公共场所分布图
        put(XIAN_SEISMIC_SCHOOL, 1 / 350000.0);    // 地震学校分布图
        put(XIAN_SEISMIC_HOSPITAL, 1 / 200000.0);   // 地震医院分布图
        put(XIAN_SEISMIC_RESCUE_MATERIAL, 1 / 400000.0); // 地震救援物资分布图
        put(XIAN_SEISMIC_TOURIST_SPOT, 1 / 250000.0);  // 地震旅游景点分布图
        put(XIAN_SEISMIC_RESERVOIR, 1 / 300000.0); // 地震水库分布图
        put(XIAN_SEISMIC_HIDE_POINT, 1 / 300000.0); // 地震隐患点分布图
        put(XIAN_SEISMIC_RISK_AREA, 1 / 150000.0); // 地震地质灾害风险区分布图
        put(XIAN_SEISMIC_PEOPLE, 1 / 300000.0);     //地震人口分布图
        put(XIAN_SEISMIC_CROPS, 1 / 300000.0);  // 地震农作物分布图


        // 暴雨专题图比例尺
        put(XIAN_STORM_FLOOD_HIDE_PEOPLE, 1 / 450000.0);   // 暴雨山洪潜在隐患点及人口分布图
        put(XIAN_STORM_FLOOD_HIDE_CROPS, 1 / 450000.0); // 暴雨山洪潜在隐患点及农作物分布图
        put(XIAN_STORM_WATER_ACCUMULATE_HIDE_PEOPLE, 1 / 200000.0); // 暴雨内涝潜在隐患点及人口分布图
        put(XIAN_STORM_WATER_ACCUMULATE_HIDE_CROPS, 1 / 200000.0); // 暴雨内涝潜在隐患点及农作物分布图
        put(XIAN_STORM_SLIDE_HIDE_PEOPLE, 1 / 250000.0); // 暴雨滑坡潜在隐患点及人口分布图
        put(XIAN_STORM_SLIDE_HIDE_CROPS, 1 / 250000.0); // 暴雨滑坡潜在隐患点及农作物分布图
        put(XIAN_STORM_FLOWS_HIDE_PEOPLE, 1 / 250000.0); // 暴雨泥石流潜在隐患点及人口分布图
        put(XIAN_STORM_FLOWS_HIDE_CROPS, 1 / 250000.0); // 暴雨泥石流潜在隐患点及农作物分布图
        put(XIAN_STORM_CITY_LIFE_LINE, 1 / 250000.0); // 暴雨城市生命线工程分布图
        put(XIAN_STORM_FLOOD_CONTROL, 1 / 400000.0); // 暴雨防汛物资分布图
        put(XIAN_STORM_RESERVOIR, 1 / 300000.0); // 暴雨附近水库分布图
        put(XIAN_STORM_RISK_AREA, 1 / 150000.0); // 暴雨地质灾害风险区分布图
        put(XIAN_STORM_SAFE_PLACE, 1 / 250000.0); // 暴雨避难场所分布图
        put(XIAN_STORM_HOSPITAL, 1 / 200000.0); // 暴雨附近医院分布图
        put(XIAN_STORM_RESCUE_TEAMS, 1 / 400000.0); // 暴雨附近救援队伍分布图
    }};

    // 烈度映射
    public static final Map<Integer, String> SEISMIC_INTENSITY_MAPPING = new HashMap<Integer, String>(){{
        put(6, "Ⅵ度");
        put(7, "Ⅶ度");
        put(8, "Ⅷ度");
        put(9, "Ⅸ度");
        put(10, "Ⅹ度");
        put(11, "Ⅺ度");
        put(12, "Ⅻ度");
    }};
}
