package com.ruoyi.common.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: xiaodemos
 * @date: 2025-05-07 18:25
 * @description:
 */


public class ReportConstants {

    public static final String CITY_YA = "雅安市政府";
    public static final String PROVINCE_SC = "四川省政府";
    public static final String KM = "公里";
    public static final String DEGREE = "度";
    public static final String NAN = "N/A";

    public static final String REGION_YC = "雨城区";
    public static final String REGION_MS = "名山区";
    public static final String REGION_YJ = "荥经县";
    public static final String REGION_HY = "汉源县";
    public static final String REGION_SM = "石棉县";
    public static final String REGION_TQ = "天全县";
    public static final String REGION_LS = "芦山县";
    public static final String REGION_BX = "宝兴县";

    public static final String NOT_IN_YA = "不在雅安市";


    public static final List<String> LOW_INTENSITY = List.of("0度", "1度", "2度", "3度");
    public static final List<String> MEDIUM_INTENSITY = List.of("4度", "5度", "6度");
    public static final List<String> HIGH_INTENSITY = List.of("7度", "8度", "9度", "10度", "11度", "12度");



    // 雅安市行政区划
    public static final String[] CITIES = {
            REGION_YC,
            REGION_MS,
            REGION_YJ,
            REGION_HY,
            REGION_SM,
            REGION_TQ,
            REGION_LS,
            REGION_BX
    };


    // 四川省雅安市特殊时段
    public static final String[][] SPACIAL_EVENT_DATA = {
            // 2023年事件
            {"2023-01", "21,22,23,24,25,26,27", "春节"},
            {"2023-01", "01,02", "元旦节"},
            {"2023-01", "10,11,12,13,14", "四川两会时段"},
            {"2023-03", "04,05,06,07,08,09,10,11,12,13", "全国两会时段"},
            {"2023-04", "05", "清明节"},
            {"2023-04", "29,30", "劳动节"},
            {"2023-05", "01,02,03", "劳动节"},
            {"2023-06", "22,23,24", "端午节"},
            {"2023-09", "29,30", "中秋节"},
            {"2023-10", "01,02,03,04,05,06", "国庆节"},

            // 2021年事件
            {"2021-01", "28,29,30,31", "四川两会时段"},
            {"2021-02", "01,02", "四川两会时段"},
            {"2021-03", "04,05,06,07,08,09,10", "全国两会时段"},
            {"2021-06", "07,08,09", "高考时段"},
            {"2021-07", "12,13,14", "雅安中考时段"},
            {"2021-08", "16,17,18,19", "雅安两会时段"},

            // 2022年事件
            {"2022-01", "01,02,03", "元旦节"},
            {"2022-01", "09,10,11,12", "雅安两会时段"},
            {"2022-01", "17,18,19,20,21", "四川两会时段"},
            {"2022-01", "31", "春节"},
            {"2022-02", "01,02,03,04,05,06", "春节"},
            {"2022-03", "04,05,06,07,08,09,10", "全国两会时段"},
            {"2022-04", "03,04,05", "清明节"},
            {"2022-04", "30", "劳动节"},
            {"2022-05", "01,02,03,04", "劳动节"},
            {"2022-06", "03,04,05", "端午节"},
            {"2022-09", "10,11,12", "中秋节"},
            {"2022-10", "01,02,03,04,05,06,07", "国庆节"},

            // 2024年事件
            {"2024-06", "07,08,09", "高考时段"},
            {"2024-06", "14,15,16", "雅安中考时段"}
    };


}
