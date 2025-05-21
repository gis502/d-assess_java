package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;

@Data
@TableName(value = "yaan_administrative_boundary")
public class YaAdministrativeBoundary {

    // 边界线Id
    @TableId(value = "yaan_boundary_id", type = IdType.NONE)
    private String yaBoundaryId;
    // 经纬度
    @TableField(value = "geom")
    private Geometry geom;

}
