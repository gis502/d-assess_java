package com.ruoyi.common.drawers.report.strategy.impl;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 10:16
 * @description: 外地地震应急响应策略
 */

@AllArgsConstructor
@NoArgsConstructor
public class ExternalResponseStrategy implements ResponseStrategy {

    private TrafficRequest request;

    @Override
    public String generateResponse(String cityOrState, String countyOrDistrict, String earthquakeName) {
        return "无";
    }

    @Override
    public String generatePlan(String cityOrState, Double eqMagnitude, String neighboringCityOrState) {
        return eqMagnitude < 6.0 ? "无" : "迅速联系" + neighboringCityOrState + "和省抗震救灾指挥部，根据需要和我市震情灾情组织必要的抢险救援队伍、"
                + "医疗救护队伍、相应技术人员和物资对灾区进行支援（我市灾情未完全调查清楚前，至少保留2/3以上队伍和物资）";
    }
    @Override
    public String generateTraffic() {
        return (request.getEqMagnitude() >= 6.0)
                ? "保障我市通往" + request.getNeighboringCityOrState() + "的国省干道畅通，确保救援车辆和物资顺利过境"
                : "无";
    }


}
