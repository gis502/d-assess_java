package com.ruoyi.system.domain.params;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2025-08-30 15:39
 * @description: 暴雨事件编码和批次编码
 */


@Data
@AllArgsConstructor
public class RainParams {

    private String rainId;
    private String rainQueueId;

}
