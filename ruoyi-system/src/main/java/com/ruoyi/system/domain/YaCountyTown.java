package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;

@Data
@TableName(value = "yaan_county_town")
public class YaCountyTown {
    /**
     * 县城id
     */
    @TableId(value = "county_town_id", type = IdType.NONE)
    private String countyTownId;

    /**
     * 县城名称
     */
    @TableField(value = "county_town_name")
    private String countyTownName;

    /**
     * 县区经纬度
     */
    @TableField(value = "geom")
    private Geometry geom;

    /**
     * 抗震设防烈度
     */
    @TableField(value = "seismic_fortification_intensity")
    private Integer seismicFortificationIntensity;
}
