package com.ruoyi.common.drawers.report.strategy;

import com.ruoyi.common.core.domain.TrafficRequest;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 10:08
 * @description: 应急响应策略接口
 */


public interface ResponseStrategy {

    // 生成响应描述
    String generateResponse(String cityOrState, String countyOrDistrict, String earthquakeName);
    // 生成救援计划策略
    String generatePlan(String cityOrState, Double eqMagnitude, String neighboringCityOrState);
    // 生成交通建议策略
    String generateTraffic();



}
