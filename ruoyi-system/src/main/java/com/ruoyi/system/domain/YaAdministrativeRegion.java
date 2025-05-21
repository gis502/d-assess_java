package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "yaan_administrative_region")
public class YaAdministrativeRegion {

    @TableId(value = "id", type = IdType.NONE)
    private Integer id;

    @TableField(value = "geom")
    private Object geom;

    @TableField(value = "adCode")
    private Integer adCode;

    @TableField(value = "name")
    private String name;

    @TableField(value = "center")
    private Integer center;

    @TableField(value = "centroid")
    private Integer centroid;

    @TableField(value = "children_num")
    private Integer childrenNum;

    @TableField(value = "level")
    private String level;

    @TableField(value = "parent")
    private Integer parent;

    @TableField(value = "sub_feature")
    private Integer subFeature;

    @TableField(value = "acRoutes")
    private Integer acRoutes;
}
