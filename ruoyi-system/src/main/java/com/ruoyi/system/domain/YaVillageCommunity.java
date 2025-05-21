package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;


/**
 * 雅安 ----  村、社区信息表
 */
@Data
@TableName(value = "yaan_village_and_community")
public class YaVillageCommunity {

    /**
     * 序号，自增主键
     */
    @TableId(value = "id", type = IdType.NONE) // 主键自动生成
    private Integer id;

    /**
     * 统计用区划代码
     */
    @TableField(value = "statistics_area_code")
    private String statisticsAreaCode;

    /**
     * 城乡分类代码
     */
    @TableField(value = "urban_and_rural_classification_code")
    private Integer urbanAndRuralClassificationCode;

    /**
     * 县（区）
     */
    @TableField(value = "county_or_district")
    private String countyOrDistrict;

    /**
     * 乡镇（街道）
     */
    @TableField(value = "township_or_street")
    private String townshipOrStreet;

    /**
     * 村（社区）
     */
    @TableField(value = "village_or_community")
    private String villageOrCommunity;

    /**
     * 户数（风普）
     */
    @TableField(value = "number_of_households_or_fengpu")
    private Integer numberOfHouseholdsOrFengpu;

    /**
     * 常住人口（风普）
     */
    @TableField(value = "resident_population_fengpu")
    private Integer residentPopulationFengpu;

    /**
     * 经纬度
     */
    @TableField(value = "geom")
    private Geometry geom; // 这里的类型需要根据实际数据库中的 `geometry` 类型来定义
}




