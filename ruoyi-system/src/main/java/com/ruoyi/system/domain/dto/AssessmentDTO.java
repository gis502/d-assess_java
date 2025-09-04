package com.ruoyi.system.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: xiaodemos
 * @date: 2025-04-05 22:27
 * @description: 评估DTO类
 */

@Data
public class AssessmentDTO implements Serializable {

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

    /**
     *地震报告评估数据
     */
    private String faultZone; // 距离震中最近断裂带
    private String circleArea; // 重灾区面积
    private String affectPopMax; // 影响人数最大值
    private String affectPopMin; // 影响人数最小值
    private String diePopMax; // 死亡人数最大值
    private String diePopMin; // 死亡人数最小值
    private List<String> country; // 影响街道
    private String intensity; // 震区烈度

}
