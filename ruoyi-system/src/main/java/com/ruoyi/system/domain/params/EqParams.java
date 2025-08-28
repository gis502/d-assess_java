package com.ruoyi.system.domain.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: xiaodemos
 * @date: 2025-04-17 21:32
 * @description: 地震事件编码和地震批次编码
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EqParams {

    private String eqId;    // 地震事件编码
    private String eqqueueId;   // 地震批次编码

}
