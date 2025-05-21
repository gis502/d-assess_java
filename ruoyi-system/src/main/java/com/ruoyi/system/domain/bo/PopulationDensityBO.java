package com.ruoyi.system.domain.bo;

import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2025-05-15 17:18
 * @description:
 */


@Data
public class PopulationDensityBO {

    // 用于存储匹配到的人口密度
    private String populationDensity;
    private String countyOrDistrict;
    private String cityOrState;
    private String newCountyOrDistrict;
    private String result;

    private int year;
    private int month;
    private int day;
    private String monthDay;
}
