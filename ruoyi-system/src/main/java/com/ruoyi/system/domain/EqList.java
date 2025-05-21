package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ruoyi.system.handler.GeometryTypeHandler;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;
import org.n52.jackson.datatype.jts.GeometryDeserializer;
import org.n52.jackson.datatype.jts.GeometrySerializer;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-04-04 11:32
 * @description: 历史地震表
 */

@Data
@TableName(value = "eq_list")
public class EqList implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableField(value = "eq_id")
    private String eqId; // 地震唯一标识符

    @TableField(value = "eqqueue_id")
    private String eqqueueId; // 队列唯一标识符

    @TableField(value = "eq_name")
    private String eqName; // 地震名称

    // 地震发生时间
    @TableField(value = "eq_time")
    private LocalDateTime eqTime;

    // 震源深度
    @TableField(value = "eq_depth")
    private Double eqDepth;

    @TableField(value = "eq_addr")
    private String eqAddr; // 震中位置

    @TableField(value = "eq_type")
    private String eqType;  // 地震类型（Z正式，Y演练，T测试）

    @TableField(value = "magnitude")
    private Double magnitude; // 震级

    @TableField(value = "intensity")
    private Integer intensity; // 地震烈度

    @TableField(value = "eq_full_name")
    private String eqFullName; // 地震全称

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT, value = "create_time")
    private LocalDateTime createTime; // 创建时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE, value = "update_time")
    private LocalDateTime updateTime; // 修改时间

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted; // 逻辑删除

    @TableField(value = "geom", typeHandler = GeometryTypeHandler.class)
    @JsonSerialize(using = GeometrySerializer.class)
    @JsonDeserialize(using = GeometryDeserializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)  // 仅序列化非空字段
    private Geometry geom; // 震中经纬度（需要导入相应的Geometry类）

}
