package com.ruoyi.common.drawers.report.strategy.impl;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 17:47
 * @description: 非雅安市三级响应策略
 */

@AllArgsConstructor
@NoArgsConstructor
public class NonYaAnLevel3Strategy implements ResponseStrategy {

    private  TrafficRequest request;

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
        return "保障通往受灾较重区域的国省干道畅通，确保救援车辆和物资顺利到达震中";
    }
}
