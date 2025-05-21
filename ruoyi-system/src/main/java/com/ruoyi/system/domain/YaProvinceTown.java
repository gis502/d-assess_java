package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;
import org.n52.jackson.datatype.jts.GeometryDeserializer;
import org.n52.jackson.datatype.jts.GeometrySerializer;

@Data
@TableName(value = "yaan_province_town")
public class YaProvinceTown {
    /**
     * 序号，自增主键
     */
    @TableId(value = "province_town_id", type = IdType.NONE)
    private String provinceTownId;

    @TableField(value = "pac")
    private String pac;

    @TableField(value = "province_town_name")
    private String provinceTownName;

    @TableField(value = "full_province_town_name")
    private String fullProvinceTownName;

    @TableField(value = "geom")
    @JsonSerialize(using = GeometrySerializer.class)
    @JsonDeserialize(using = GeometryDeserializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)  // 仅序列化非空字段
    private Geometry geom;

}
