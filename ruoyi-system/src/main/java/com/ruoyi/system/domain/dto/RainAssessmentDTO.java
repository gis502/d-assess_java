package com.ruoyi.system.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-04-05 22:27
 * @description: 评估DTO类
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RainAssessmentDTO implements Serializable {

    private String rainId;
    private String rainQueueId;
    private String rainName;    // 暴雨名称
    private String position;    // 区县
    private double longitude;   // 经度
    private double latitude;    // 纬度
    private String rainfall;    // 降雨量
    private String duration;    // 已持续时间
    private String rainType;    // 暴雨类型
    private LocalDateTime occurrenceTime;   // 发生时间


}
