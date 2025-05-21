package com.ruoyi.common.core.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-05-12 10:32
 * @description: 评估业务BTO类
 */


@Data
public class AssessmentBTO {

    private String eqId;    // 地震编码
    private String eqqueueId;   // 评估编码
    private String eqName;  // 地震名称
    private String eqAddr;  //震发位置
    private LocalDateTime eqTime;   // 地震时间
    private double longitude;   // 经度
    private double latitude;    // 纬度
    private double eqDepth;   // 震源深度
    private double magnitude;   // 震级
    private String eqType;  // 地震类型



}
