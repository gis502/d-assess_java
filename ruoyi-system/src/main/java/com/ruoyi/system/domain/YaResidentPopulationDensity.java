package com.ruoyi.system.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 全省常住人口密度
 */

@Data
@TableName(value = "yaan_population_density")
public class YaResidentPopulationDensity {
    /**
     * 序号，自增主键
     */
    @TableId(value = "id", type = IdType.NONE) // 主键自动生成用： type = IdType.AUTO
    private Integer id;
    /**
     * 原区划代码排序序号
     */
    @TableField(value = "original_number")
    private String originalNumber;
    /**
     * 区划代码
     */
    @TableField(value = "zoning_code")
    private String zoningCode;
    /**
     * 市/州
     */
    @TableField(value = "city_or_state")
    private String cityOrState;
    /**
     * 县/区
     */
    @TableField(value = "county_or_district")
    private String countyOrDistrict;
    /**
     * 市+县
     */
    @TableField(value = "city_and_county")
    private String cityAndCounty;
    /**
     * 常住人口（万人）
     * 数据：2020年
     */
    @TableField(value = "permanent_population")
    private String permanentPopulation;
    /**
     * 面积（平方公里）
     * 数据：2019年
     */
    @TableField(value = "area")
    private String area;
    /**
     * 人口密度（人/平方公里）
     */
    @TableField(value = "population_density")
    private String populationDensity;
    /**
     * 县区简介
     */
    @TableField(value = "county_introduction")
    private String countyIntroduction;
}
