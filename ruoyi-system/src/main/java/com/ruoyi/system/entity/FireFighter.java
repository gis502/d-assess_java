package com.ruoyi.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author zzw
 * @description: 救援队伍实体
 * @date 2025/10/20 下午9:52
 */
@Data
@TableName("xian_firefighter")
public class FireFighter {

    @TableId
    private Integer id;
    @TableField("team_name")
    private String teamName;
    @TableField("team_type")
    private String teamType;
    @TableField("fire_type")
    private String fireType;
    @TableField("address")
    private String address;
    @TableField("team_num")
    private Integer teamNum;
    @TableField("cars")
    private Integer fireCars;
    @TableField("devices")
    private Integer fireDevices;
    @TableField("unit_head")
    private String unitHead;
    @TableField("telephone")
    private String telephone;
    @TableField("province")
    private String province;
    @TableField("city")
    private String city;
    @TableField("county")
    private String county;
    @TableField("country")
    private String country;
    @TableField("lon")
    private Double longitude;
    @TableField("lat")
    private Double latitude;
    /**位置（几何类型）*/
    @TableField("point")
    private String point;

}
