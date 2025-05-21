package com.ruoyi.common.drawers.report.strategy.impl;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 17:38
 * @description: 邻近市州地震策略
 */

@AllArgsConstructor
@NoArgsConstructor
public class NeighboringCityStrategy implements ResponseStrategy {

    private TrafficRequest request;

    @Override
    public String generateResponse(String cityOrState, String countyOrDistrict, String earthquakeName) {
        return null;
    }

    @Override
    public String generatePlan(String cityOrState, Double eqMagnitude, String neighboringCityOrState) {
        return null;
    }

    @Override
    public String generateTraffic() {
        // 震级高于六级
        if (request.getEqMagnitude() >= 6.0) {
            return "保障我市通往" + request.getNeighboringCityOrState() + "的国省干道畅通，确保救援车辆和物资顺利过境";
        }
        return "无";

    }

}
