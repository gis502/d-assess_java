package com.ruoyi.system.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: xiaodemos
 * @date: 2025-04-08 15:32
 * @description: 绘制布局的文本信息
 */


@Data
public class DrawersInfoBO {

    private String title;
    private String picName;
    private String eqAddr;
    private String makeTime;  // 绘图时间
    private double magnitude;
    private String eqTime;

    private Integer layoutId;  // 布局名称

    private String eqqueueId;

}
