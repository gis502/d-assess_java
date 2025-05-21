package com.ruoyi.common.drawers.report;

import com.ruoyi.common.constant.ReportConstants;
import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ruoyi.common.drawers.report.SpecialEventsResponseHandle.getEventForDate;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 9:42
 * @description: 应急响应决策
 */


public class EmergencyResponseDecider {


    /**
     * 根据市/州与响应级别生成地震应急响应描述
     *
     * @param cityOrState       城市或州名称（用于判断是否为雅安市）
     * @param suggestion        应急响应建议级别
     * @param outside           外省最大的烈度
     * @param big               >7级以上的烈度区域总数
     * @param middle            >6级以上的烈度区域总数
     * @param small             >5级以上的烈度区域总数
     * @param eqMagnitude       震级
     * @param countyOrDistrict  行政区划
     * @param populationDensity 人口密度描述
     * @return 返回一个响应描述
     */
    public static String generateEarthquakeResponse(String cityOrState, String suggestion,
                                                    int outside, double big, double middle, double small, double eqMagnitude,
                                                    String countyOrDistrict, String populationDensity) {
        StringBuilder response = new StringBuilder();

        if (!"雅安市".equals(cityOrState)) {
            appendNonYaAnResponse(response, suggestion, outside, big, middle, small, eqMagnitude);
        } else {
            appendYaAnResponse(response, eqMagnitude, countyOrDistrict, populationDensity);
        }

        return response.toString();
    }

    /**
     * 生成地震应急响应建议的处置措施（指挥部建议）
     *
     * @param cityOrState      城市或州名称（用于判断是否为雅安市）
     * @param suggestion       应急响应建议级别
     * @param countyOrDistrict 县/区名称（用于具体定位）
     * @param earthquakeName   地震名称（用于指挥部命名）
     * @return 对应的应急响应处置措施
     */
    public static String earthquakeResponse(String cityOrState, String suggestion,
                                            String countyOrDistrict, String earthquakeName) {
        // 使用策略模式处理不同响应级别
        ResponseStrategy strategy = ResponseStrategyFactory.createStrategy(suggestion);
        return strategy.generateResponse(cityOrState, countyOrDistrict, earthquakeName);
    }

    /**
     * 根据地震烈度确定灾情收集策略（灾情收集建议）
     *
     * @param maxIntensityWithUnit  最大烈度（带单位，如"7度"）
     * @param maximumIntensityPoint 最大烈度点位置描述
     * @return 灾情收集策略指令
     */
    public static String disasterCollectionFunction(String maxIntensityWithUnit,
                                                    String maximumIntensityPoint) {
        // 0-3级烈度
        if (ReportConstants.LOW_INTENSITY.contains(maxIntensityWithUnit)) {
            return "无";
        }
        // 4-6级烈度
        if (ReportConstants.MEDIUM_INTENSITY.contains(maxIntensityWithUnit)) {
            return "迅速组织收集核实我市灾情，重点关注" + maximumIntensityPoint + "老旧房屋情况";
        }
        // 7-12级烈度
        if (ReportConstants.HIGH_INTENSITY.contains(maxIntensityWithUnit)) {
            return "迅速组织收集核实我市灾情情况；协调各技术力量对 “信息孤岛” 进行灾情分析";
        }

        return "无";
    }

    /**
     * 生成救援支持方案（应急支援建议）
     *
     * @param cityOrState            城市/州名称
     * @param eqMagnitude            地震震级
     * @param neighboringCityOrState 邻近城市/州名称
     * @param suggestion             应急响应建议
     * @return 救援支持方案
     */
    public static String supportFunction(String cityOrState, Double eqMagnitude,
                                         String neighboringCityOrState, String suggestion) {
        // 使用策略模式处理不同响应级别
        ResponseStrategy strategy = ResponseStrategyFactory.createStrategy(suggestion);
        return strategy.generatePlan(cityOrState, eqMagnitude, neighboringCityOrState);
    }

    /**
     * @param sortedList 待排序列表
     * @description: 交通处置建议（1）前置 C7-C14,E7-E14
     * @author: xiaodemos
     * @date: 2025/5/9 11:23
     */
    public static String generateDestroy(List<Map.Entry<String, Double>> sortedList) {
        // 没有 8个长度的元素则不需要填写交通处置建议
        if (sortedList.size() < 8) {
            return "无";
        }

        // 获取最高烈度和最低烈度
        double maxIntensity = sortedList.get(0).getValue();  // E7
        double minIntensity = sortedList.get(7).getValue();  // E14

        // 烈度是否属于全境范围
        if (maxIntensity > 5 && minIntensity > 5) {
            return "全境";
        }

        // 选取烈度 >5 的乡镇 用、连接
        List<String> validTowns = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedList) {
            // 烈度 >5 的乡镇
            if (entry.getValue() > 5) {
                // 取前三个字符
                validTowns.add(entry.getKey().substring(0, Math.min(3, entry.getKey().length())));
            }
        }
        // 没有符合条件的乡镇，则返回无，否则返回乡镇信息
        return validTowns.isEmpty() ? "无" : String.join("、", validTowns);
    }

    /**
     * @param sortedList           待排序列表
     * @param sortedList2          待排序列表
     * @param maxIntensityWithUnit 最大烈度
     * @description: 交通处置建议（2）前置 G7-G14,I7-I14
     * @author: xiaodemos
     * @date: 2025/5/9 11:33
     * 注：
     * 如果最大烈度点在乡镇，而非县城（一般本地地震），最大烈度点选择乡镇
     * 如果全市都达到最大烈度，那么全境（判断技巧第一个E7等于最大烈度和E14等于最大烈度）
     * 如果县区的最大烈度等于最大烈度，最大烈度为**县、**县，否则为**乡镇。
     */
    public static String generateDestroy2(List<Map.Entry<String, Double>> sortedList,
                                          List<Map.Entry<String, Double>> sortedList2,
                                          double maxIntensityWithUnit) {
        // 存储符合最大烈度的县区（只取前三个字符）
        List<String> maxIntensityCounties = new ArrayList<>();
        // 存储符合最大烈度的乡镇（完整名称）
        List<String> maxIntensityTowns = new ArrayList<>();

        // 遍历县区列表，筛选出达到最大烈度的县区
        for (Map.Entry<String, Double> entry : sortedList) {
            if (entry.getValue() == maxIntensityWithUnit) {
                // 取前三个字符
                maxIntensityCounties.add(entry.getKey().substring(0, Math.min(3, entry.getKey().length())));
            }
        }

        // 遍历乡镇列表，筛选出达到最大烈度的乡镇
        for (Map.Entry<String, Double> entry : sortedList2) {
            if (entry.getValue() == maxIntensityWithUnit) {
                // 完整的乡镇名称
                maxIntensityTowns.add(entry.getKey());
            }
        }

        // 如果所有县区和乡镇的烈度都等于最大烈度，则返回 全境
        if (maxIntensityCounties.size() == sortedList.size() && maxIntensityTowns.size() == sortedList2.size()) {
            return "全境";
        }

        // 优先选择乡镇作为最大烈度点
        if (!maxIntensityTowns.isEmpty()) {
            return String.join("、", maxIntensityTowns);
        }

        // 如果没有符合条件的乡镇，则选择县区
        if (!maxIntensityCounties.isEmpty()) {
            return String.join("、", maxIntensityCounties);
        }

        // 如果没有符合最大烈度的县区和乡镇，则返回 无
        return "无";
    }

    /**
     * 生成交通应急响应方案（交通处置建议最终结果）
     *
     * @param cityOrState            市州名称
     * @param eqMagnitude            震级
     * @param neighboringCityOrState 邻近市州名称
     * @param countyOrDistrict       行政区划名称
     * @param suggestion             应急响应建议
     * @param category               是否邻近市州地震
     * @param destroy                道路损害地点
     * @param maximumIntensityPoint  市州名称
     * @return 交通管制方案
     */
    public static String transportationFunction(String cityOrState, Double eqMagnitude, String neighboringCityOrState,
                                                String countyOrDistrict, String suggestion, String category, String destroy,
                                                String maximumIntensityPoint) {

        TrafficRequest request = new TrafficRequest();
        request.setCityOrState(cityOrState);
        request.setSuggestion(suggestion);
        request.setEqMagnitude(eqMagnitude);
        request.setCountyOrDistrict(countyOrDistrict);
        request.setNeighboringCityOrState(neighboringCityOrState);
        request.setCategory(category);
        request.setDestroy(destroy);
        request.setMaximumIntensityPoint(maximumIntensityPoint);
        // 根据不同的等级生成不同应急策略
        ResponseStrategy strategy = ResponseStrategyFactory.trafficStrategy(request);
        return strategy.generateTraffic();
    }

    /**
     * @param maxIntensityWithUnit 最大烈度
     * @description: 危险源处置建议
     * @author: xiaodemos
     * @date: 2025/5/9 18:20
     */
    public static String generateResponse(String maxIntensityWithUnit) {

        // 0-3级烈度
        if (ReportConstants.LOW_INTENSITY.contains(maxIntensityWithUnit)) {
            return "无";
        }
        // 4-6级烈度
        if (ReportConstants.MEDIUM_INTENSITY.contains(maxIntensityWithUnit)) {
            return "核查我市地灾隐患点、防洪堤坝、危化企业等次生灾害源安全并及时处置";
        }
        // 7-12级烈度
        if (ReportConstants.HIGH_INTENSITY.contains(maxIntensityWithUnit)) {
            return "控制灾区危险源，封锁危险场所，核查我市地灾隐患点、防洪堤坝、危化企业等次生灾害源安全并及时处置";
        }
        // 无效响应
        return "无";
    }

    /**
     * @description: 人员伤亡和财产损失估算
     * @param maxIntensityWithUnit 最大烈度
     * @author: xiaodemos
     * @date: 2025/5/9 18:48
     */
    public static String generateImpactMessage(String maxIntensityWithUnit) {

        // 0-3级烈度
        if (ReportConstants.LOW_INTENSITY.contains(maxIntensityWithUnit)) {
            return "不会造成人员伤亡和财产损失";
        }
        // 4-12级烈度
        switch (maxIntensityWithUnit) {
            case "4度": return "一般不会造成人员伤亡和财产损失";
            case "5度": return "一般不会造成人员伤亡，但可能会有少量财产损失";
            case "6度": return "一般不会造成人员死亡，但可能会有个别人员受伤和少量财产损失";
            case "7度": return "一般会造成少量人员伤亡和部分财产损失";
            case "8度": return "一般会造成少量人员死亡和大量人员受伤及财产损失";
            case "9度":
            case "10度":
            case "11度":
            case "12度": return "一般会造成大量人员伤亡和财产损失";
            default: return "未知最大烈度/最大烈度不是整数";
        }
    }

    // TODO 在 file 中直接调用 getEventForDate(year, month, day);
    // TODO 在 file 中直接调用 generateAdvice(cityOrState, event, suggestion, maxIntensity, eqMagnitude);

    // 处置措施建议
    public static String generateSuggestion(String P31, String... values) {

        StringBuilder suggestion = new StringBuilder();
        // 记录是否有有效措施
        boolean hasValidMeasure = false;
        // 记录有效数据的数量
        int validCount = 0;

        // 处理 G31-N31 的内容
        for (int i = 0; i < values.length; i++) {
            if (!"无".equals(values[i])) {
                if (validCount > 0) {
                    // 多个有效数据之间用分号分隔
                    suggestion.append("；");
                }
                suggestion.append(values[i]);
                // 标记有效措施
                hasValidMeasure = true;
                // 有效数据数量计数
                validCount++;
            }
        }

        // 若没有有效措施，则直接返回 P31
        if (!hasValidMeasure) {
            return P31;
        }

        // 若有有效措施，添加句号并拼接 P31
        suggestion.append("。");

        return suggestion.append(P31).toString();
    }

    // 生成非雅安市响应
    private static void appendNonYaAnResponse(StringBuilder response, String suggestion,
                                              int outside, double big, double middle, double small, double eqMagnitude) {

        response.append(String.format(" 2、我市最大地震烈度：%d度；", outside));

        switch (suggestion) {
            case "启动市级地震灾害一级应急响应":
                if (big >= 1) {
                    response.append(String.format("3、7度及以上涉及雅安 %d 个县（区）。", (int) big));
                }
                break;

            case "启动市级地震灾害二级应急响应":
                if (middle >= 1) {
                    response.append(String.format("3、6度及以上涉及雅安 %d 个县（区）；", (int) middle));
                }
                if (big >= 1) {
                    response.append(String.format("4、7度及以上涉及雅安 %d 个县（区）。", (int) big));
                }
                break;

            case "启动市级地震灾害三级应急响应":
                if (small >= 1) {
                    response.append(String.format("3、5度及以上涉及雅安 %d 个县（区）；", (int) small));
                }
                if (middle >= 1) {
                    response.append(String.format("4、6度及以上涉及雅安 %d 个县（区）。", (int) middle));
                }
                break;

            case "启动强有感地震应急响应":
                if (small >= 1) {
                    response.append(String.format("3、5度及以上涉及雅安 %d 个县（区）。", (int) small));
                }
                break;

            case "启动外地地震应急响应":
                response.append(String.format("3、震级：%.1f级；4、我市最大地震烈度：%d度。", eqMagnitude, outside));
                break;
        }
    }

    // 生成雅安市响应
    private static void appendYaAnResponse(StringBuilder response, double eqMagnitude, String countyOrDistrict, String populationDensity) {

        response.append(String.format(
                " 2、震级：%.1f级；3、震中%s人口密度约%s人/平方公里。",
                eqMagnitude,
                countyOrDistrict,
                populationDensity
        ));
    }

}
