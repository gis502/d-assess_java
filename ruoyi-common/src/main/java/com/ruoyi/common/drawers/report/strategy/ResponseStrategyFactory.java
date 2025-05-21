package com.ruoyi.common.drawers.report.strategy;

import com.ruoyi.common.core.domain.TrafficRequest;
import com.ruoyi.common.drawers.report.strategy.ResponseStrategy;
import com.ruoyi.common.drawers.report.strategy.impl.*;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 10:21
 * @description: 响应策略工厂类
 */


public class ResponseStrategyFactory {

    // 应急支援、指挥部建议策略
    public static ResponseStrategy createStrategy(String suggestion) {
        switch (suggestion) {
            case "启动强有感地震应急响应":
                return new StrongFeltResponseStrategy();
            case "启动市级地震灾害三级应急响应":
                return new Level3ResponseStrategy();
            case "启动市级地震灾害二级应急响应":
                return new Level2ResponseStrategy();
            case "启动市级地震灾害一级应急响应":
                return new Level1ResponseStrategy();
            case "启动外地地震应急响应":
                return new ExternalResponseStrategy();
            case "不启动地震应急响应":
            default:
                return new NoResponseStrategy();
        }
    }

    // 交通建议策略
    public static ResponseStrategy trafficStrategy(TrafficRequest request) {

        // 针对雅安市进行响应
        if ("雅安市".equals(request.getCityOrState())) {
            switch (request.getSuggestion()) {
                case "启动市级地震灾害三级应急响应":
                    return new Level3ResponseStrategy();
                case "启动市级地震灾害二级应急响应":
                    return new Level2ResponseStrategy();
                case "启动市级地震灾害一级应急响应":
                    return new Level1ResponseStrategy();
                default:
                    return new NoResponseStrategy();
            }
            // 针对非雅安市地区进行响应
        } else {
            switch (request.getSuggestion()) {
                case "启动市级地震灾害三级应急响应":
                    return new NonYaAnLevel3Strategy(request);
                case "启动市级地震灾害二级应急响应":
                    return new NonYaAnLevel2Strategy(request);
                case "启动市级地震灾害一级应急响应":
                    return new NonYaAnLevel1Strategy(request);
                case "启动强有感地震应急响应":
                case "启动外地地震应急响应":
                    if ("邻近市州地震".equals(request.getCategory())) {
                        return new NeighboringCityStrategy(request);
                    }
                    return new NoResponseStrategy();
                default:
                    return new NoResponseStrategy();
            }
        }
    }

}
