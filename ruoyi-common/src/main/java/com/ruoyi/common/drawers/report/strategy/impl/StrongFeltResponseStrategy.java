package com.ruoyi.common.drawers.report.strategy.impl;



/**
 * @author: xiaodemos
 * @date: 2025-05-09 10:10
 * @description:
 */
import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;

/**
 * 强有感地震应急响应策略
 */
public class StrongFeltResponseStrategy implements ResponseStrategy {

    private TrafficRequest request;

    @Override
    public String generateResponse(String cityOrState, String countyOrDistrict, String earthquakeName) {
        if ("雅安市".equals(cityOrState)) {
            return "根据工作需要，请市政府指派一位市领导带领相关部门前往" + countyOrDistrict + "指导抗震救灾工作";
        }
        return "无";
    }

    // 应急救援策略
    @Override
    public String generatePlan(String cityOrState, Double eqMagnitude, String neighboringCityOrState) {
        return eqMagnitude < 6.0 ? "无" : createSupportPlan(neighboringCityOrState);
    }

    @Override
    public String generateTraffic( ) {
        return (request.getEqMagnitude() >= 6.0)
                ? "保障我市通往" + request.getNeighboringCityOrState() + "的国省干道畅通，确保救援车辆和物资顺利过境"
                : "无";
    }

    private String createSupportPlan(String neighboringCityOrState) {
        return "迅速联系" + neighboringCityOrState + "和省抗震救灾指挥部，根据需要和我市震情灾情组织必要的抢险救援队伍、"
                + "医疗救护队伍、相应技术人员和物资对灾区进行支援（我市灾情未完全调查清楚前，至少保留2/3以上队伍和物资）";
    }

}

