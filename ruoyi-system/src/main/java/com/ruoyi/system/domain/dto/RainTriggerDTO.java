package com.ruoyi.system.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-08-30 13:53
 * @description:
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RainTriggerDTO {

    private String position;    // 区县
    private double longitude;   // 经度
    private double latitude;    // 纬度
    private String rainfall;    // 降雨量
    private String duration;    // 已持续时间
    private String rainType;    // 暴雨类型
    private LocalDateTime occurrenceTime;   // 发生时间


}
