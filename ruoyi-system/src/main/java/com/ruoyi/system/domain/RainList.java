package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.system.handler.GeometryTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-08-30 15:25
 * @description: 暴雨灾害列表
 */


@Data
@TableName("rain_list")
@AllArgsConstructor
@NoArgsConstructor
public class RainList {

    @TableId
    @TableField("id")
    private Long Id;
    @TableField("rain_id")
    private String rainId;
    @TableField("rain_queue_id")
    private String rainQueueId;
    @TableField("disaster_name")
    private String disasterName;
    @TableField("position")
    private String position;
    @TableField("occurrence_time")
    private LocalDateTime occurrenceTime;
    @TableField("rainfall")
    private String rainfall;
    @TableField("duration")
    private String duration;
    @TableField(value = "geom", typeHandler = GeometryTypeHandler.class)
    private Geometry geom; //经纬度
    @TableField("longitude")
    private Double longitude;
    @TableField("latitude")
    private Double latitude;
    @TableField("rain_type")
    private String rainType;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableField("is_deleted")
    private Integer isDeleted;

}
