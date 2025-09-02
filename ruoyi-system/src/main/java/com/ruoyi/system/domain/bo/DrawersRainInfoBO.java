package com.ruoyi.system.domain.bo;

import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2025-04-08 15:32
 * @description: 绘制布局的文本信息
 */


@Data
public class DrawersRainInfoBO {

    private String title;
    private String picName;
    private String rainAddr;
    private String makeTime;  // 绘图时间
    private String duration;
    private String rainfall;
    private String rainTime;
    private Integer layoutId;  // 布局名称
    private String rainQueueId;

}
