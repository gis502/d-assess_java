package com.ruoyi.system.domain.bo;

import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2025-05-15 17:34
 * @description:
 */


@Data
public class SeismicCategoryBO {

    private String category;  //D31
    private int maxIntensity ;   //  最大烈度 初始为0       B20 无"度"字
    private int outside;//   辖区外地震雅安最大烈度     F19
    //如果是不是雅安市内的
    private double big; //外地地震雅安最大烈度8度及以上时，筛选7度及以上县个数   J26
    private double middle; //外地地震雅安最大烈度7度及以上时，筛选6度、7度县个数  K26
    private double small;  //外地地震雅安最大烈度6度及以上时，筛选5度、6度县个数   L26
    private String panduan;
    private String maxIntensityWithUnit;
}
