package com.ruoyi.common.constant;

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

    public static final String YA_AN_AREA_CODE = "511800";  // 雅安市行政区划代码

    public static final String FILE_CREATE_FILED = "文件夹创建失败";

    public static final String TRIGGER_FILED = "地震启动失败";
    public static final String TRIGGER_ERROR = "地震启动异常";
    public static final String TRIGGER_SUCCESS = "地震启动成功";

    public static final String REASSESSMENT_FILED = "地震重新评估失败";
    public static final String REASSESSMENT_ERROR = "地震重新评估异常";
    public static final String REASSESSMENT_SUCCESS = "地震重新评估成功";

    public static final String QUERY_PARAMS_ERROR = "地图输出失败";
    public static final String THEMATIC_MAP_ERROR= "专题图获取失败";

    public static final String BASE_INFO_ERROR = "获取数据失败";

    public static final String FULL_NAME_SUFFIX = "级地震";

    public static final String PARAMETER_ERROR = "参数异常";

    public static final String PARSE_ERROR = "解析失败";

    public static final String DATASETS_NAME = "测试数据源";
    public static final String WORKSPACE_PATH = "E:/GIS小组专题图产出/专题图模板/专题图.smwu";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String FILE_NOT_FOUND_ERROR = "文件不存在";
    public static final String HTTP_PREFIX = ""; // http://

}
