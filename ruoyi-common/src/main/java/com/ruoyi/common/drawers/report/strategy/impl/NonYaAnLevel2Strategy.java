package com.ruoyi.common.drawers.report.strategy.impl;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 17:45
 * @description: 非雅安市二级应急响应策略
 */

@AllArgsConstructor
@NoArgsConstructor
public class NonYaAnLevel2Strategy implements ResponseStrategy {

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
        return "迅速对通往" + request.getMaximumIntensityPoint() +
                "的国省干道进行必要的交通管制，保障救援车辆、机械和人员优先通行，"
                + "其他救灾车辆和人员调节通行，根据需要启用直升机起降场地";
    }
}
