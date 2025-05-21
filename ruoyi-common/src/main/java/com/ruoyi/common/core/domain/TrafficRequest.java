package com.ruoyi.common.core.domain;

import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2025-05-09 11:50
 * @description: 交通建议BO类
 */

@Data
public class TrafficRequest {

    private String cityOrState;
    private Double eqMagnitude;
    private String neighboringCityOrState;
    private String countyOrDistrict;
    private String suggestion;
    private String category;
    private String destroy;
    private String maximumIntensityPoint;

}
