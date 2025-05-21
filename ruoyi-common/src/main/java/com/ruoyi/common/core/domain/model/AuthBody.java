package com.ruoyi.common.core.domain.model;

import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2025-04-18 11:09
 * @description: 验证体
 */

@Data
public class AuthBody {

    private String username;
    private String password;
    private String token;
    private Long expire;
}
