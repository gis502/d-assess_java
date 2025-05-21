package com.ruoyi.common.drawers.report;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 9:47
 * @description:
 */


public class PopulationEconomicImpactAnalyzer {

    /**
     * 生成一个结果字符串，包含 ±30% 浮动的灾难影响估计。
     *
     * @param totalDeath          死亡人数
     * @param injury              受伤人数
     * @param totalEconomicLoss   经济损失
     * @param totalBuildingDamage 建筑物损失
     * @return 返回一个评估结果
     */
    public static String generateDisasterImpactEstimate(int totalDeath, int injury, double totalEconomicLoss, double totalBuildingDamage) {
        return String.format("预估全市%s，%s，%s，%s。",
                formatDeathEstimate(totalDeath),
                formatInjuryEstimate(injury),
                formatBuildingDamageEstimate(totalBuildingDamage),
                formatEconomicLossEstimate(totalEconomicLoss));
    }

    // 死亡人数评估
    private static String formatDeathEstimate(int totalDeath) {
        return totalDeath > 0 ?
                String.format("死亡%d（按±30%%浮动）人", totalDeath) :
                "死亡X～X（按±30%浮动）人";
    }

    // 伤亡人数评估
    private static String formatInjuryEstimate(int injury) {
        return injury > 0 ?
                String.format("受伤%d（按±30%%浮动）人", injury) :
                "受伤X～X（按±30%浮动）人";
    }

    // 房屋破坏评估
    private static String formatBuildingDamageEstimate(double totalBuildingDamage) {
        return totalBuildingDamage > 0 ?
                String.format("总建筑破坏面积%.2f（按±30%%浮动）平方公里", totalBuildingDamage) :
                "房屋损毁X～X（按±30%浮动）间";
    }

    // 经济损失评估
    private static String formatEconomicLossEstimate(double totalEconomicLoss) {
        return totalEconomicLoss > 0 ?
                String.format("直接经济损失%.2f亿元（按±30%%浮动）", totalEconomicLoss) :
                "直接经济损失X～X亿元（按±30%浮动）";
    }

}
