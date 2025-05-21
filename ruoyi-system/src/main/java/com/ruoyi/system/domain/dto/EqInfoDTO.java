package com.ruoyi.system.domain.dto;

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

import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-04-18 12:53
 * @description: 历史地震基本信息
 */

@Data
public class EqInfoDTO {

    private String eqId;             // 地震唯一标识符
    private String eqqueueId;        // 队列唯一标识符
    private String eqName;           // 地震名称
    private String eqTime;   // 地震发生时间
    private Double eqDepth;         // 震源深度
    private String eqAddr;          // 震中位置
    private String eqType;          // 地震类型（Z正式，Y演练，T测试）
    private Double magnitude;       // 震级
    private Integer intensity;      // 地震烈度
    private String eqFullName;     // 地震全称
    private Double longitude;
    private Double latitude;
}
