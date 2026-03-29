package com.ruoyi.disaster.file.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 灾害信息 DTO
 * 用于返回暴雨和地震的基本信息
 * 
 * @author ruoyi
 * @date 2026-03-29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisasterInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 灾害 ID（暴雨 rain_id 或地震 disaster_id）
     */
    private String disasterId;

    /**
     * 灾害名称
     */
    private String disasterName;

    /**
     * 灾害类型（rain:暴雨，earthquake:地震）
     */
    private String disasterType;

    /**
     * 发生时间
     */
    private LocalDateTime occurrenceTime;

    /**
     * 位置信息
     */
    private String position;

    /**
     * 附加信息（暴雨为降雨量，地震为震级）
     */
    private String extraInfo;
}
