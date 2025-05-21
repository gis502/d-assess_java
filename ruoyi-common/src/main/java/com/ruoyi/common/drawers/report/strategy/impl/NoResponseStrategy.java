package com.ruoyi.common.drawers.report.strategy.impl;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 10:09
 * @description: 不启动响应策略
 */

@AllArgsConstructor
@NoArgsConstructor
public class NoResponseStrategy implements ResponseStrategy {

    private TrafficRequest request;

    @Override
    public String generateResponse(String cityOrState, String countyOrDistrict, String earthquakeName) {
        return "无";
    }

    @Override
    public String generatePlan(String cityOrState, Double eqMagnitude, String neighboringCityOrState) {
        return "无";
    }

    @Override
    public String generateTraffic() {
        return "无";
    }

}
