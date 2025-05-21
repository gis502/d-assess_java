package com.ruoyi.common.drawers.report;

import com.ruoyi.common.constant.ReportConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 19:05
 * @description: 四川省雅安市每年特殊时期
 */

public class SpecialEventsResponseHandle {

    // 事件映射表缓存
    private static final Map<String, Set<String>> EVENT_MAP = initEventMap();

    // 定义响应级别判断条件
    private static final BiPredicate<String, Double> IS_YA_HOLIDAY_CONDITION =
            (city, magnitude) -> "雅安市".equals(city) && magnitude > 3.9;

    private static final BiPredicate<String, String> IS_NON_YA_HOLIDAY_CONDITION =
            (city, suggestion) -> !"雅安市".equals(city) && isSignificantResponse(suggestion);

    // 定义疫情高发期强度判断
    private static final String[] SIGNIFICANT_INTENSITIES = {
            "4度", "5度", "6度", "7度", "8度", "9度", "10度", "11度", "12度"
    };

    // TODO 节假日建议模板
    private static final Map<String, String> HOLIDAY_ADVICE_TEMPLATES = new HashMap<>();
    static {
        HOLIDAY_ADVICE_TEMPLATES.put("春节", "正值%s节假日，应特别关注%s景区及人员密集场所情况。");
        HOLIDAY_ADVICE_TEMPLATES.put("清明节", "正值%s节假日，应特别关注%s景区及人员密集场所情况。");
        HOLIDAY_ADVICE_TEMPLATES.put("劳动节", "正值%s节假日，应特别关注%s景区及人员密集场所情况。");
    }


    /**
     * 生成特殊时段处置建议
     */
    public static String generateAdvice(String cityOrState, String event, String suggestion, String maxIntensity, double eqMagnitude) {
        StringBuilder advice = new StringBuilder();

        // 处理节假日情况
        if (HOLIDAY_ADVICE_TEMPLATES.containsKey(event)) {
            handleHolidayAdvice(advice, cityOrState, event, suggestion, eqMagnitude);
        }

        // 处理疫情高发期情况
        if ("冠状病毒疫情高发期".equals(event)) {
            handlePandemicAdvice(advice, cityOrState, maxIntensity, eqMagnitude);
        }

        return advice.toString();
    }

    public static String getEventForDate(String year, String month, String day) {
        // 构造 key，查询事件
        String key = year + "-" + month;
        if (EVENT_MAP.containsKey(key)) {
            for (String entry : EVENT_MAP.get(key)) {
                if (entry.startsWith(day + "|")) {
                    return entry.split("\\|")[1]; // 获取事件名称
                }
            }
        }
        return "";
    }

    // 初始化事件映射表
    private static Map<String, Set<String>> initEventMap() {
        Map<String, Set<String>> map = new HashMap<>();
        for (String[] event : ReportConstants.SPACIAL_EVENT_DATA) {
            String key = event[0];
            String[] days = event[1].split(",");
            String eventName = event[2];
            addEvent(map, key, days, eventName);
        }
        return map;
    }

    private static void addEvent(Map<String, Set<String>> eventMap, String key, String[] days, String eventName) {
        eventMap.putIfAbsent(key, new HashSet<>());
        for (String day : days) {
            eventMap.get(key).add(day + "|" + eventName);
        }
    }

    private static void handleHolidayAdvice(StringBuilder advice, String cityOrState,
                                            String event, String suggestion,
                                            double magnitude) {
        String template = HOLIDAY_ADVICE_TEMPLATES.get(event);

        if (IS_YA_HOLIDAY_CONDITION.test(cityOrState, magnitude)) {
            advice.append(String.format(template, event, "震中附近"));
        } else if (IS_NON_YA_HOLIDAY_CONDITION.test(cityOrState, suggestion)) {
            advice.append(String.format(template, event, "我市强有感以上区域"));
        }
    }

    private static void handlePandemicAdvice(StringBuilder advice, String cityOrState,
                                             String maxIntensity, double magnitude) {
        if ("雅安市".equals(cityOrState) && magnitude > 3.9) {
            advice.append("正值新冠肺炎防控期，应特别关注我市市区、震中城区和震中附近乡镇人员动向情况，" +
                    "提醒应急处置人员和户外避险群众做好个人防护。");
        } else if (!"雅安市".equals(cityOrState) && isSignificantIntensity(maxIntensity)) {
            advice.append("正值新冠肺炎防控期，应特别关注我市有感以上区域主要乡镇、街道人员动向情况，" +
                    "提醒应急处置人员和户外避险群众做好个人防护。");
        }
    }

    private static boolean isSignificantResponse(String suggestion) {
        return "启动强有感地震应急响应".equals(suggestion) ||
                "启动市级地震灾害三级应急响应".equals(suggestion) ||
                "启动市级地震灾害二级应急响应".equals(suggestion) ||
                "启动市级地震灾害一级应急响应".equals(suggestion);
    }

    private static boolean isSignificantIntensity(String intensity) {
        for (String sigIntensity : SIGNIFICANT_INTENSITIES) {
            if (sigIntensity.equals(intensity)) {
                return true;
            }
        }
        return false;
    }

}
