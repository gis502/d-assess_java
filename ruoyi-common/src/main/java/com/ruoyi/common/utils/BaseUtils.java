package com.ruoyi.common.utils;

import com.ruoyi.common.constant.BaseConstants;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: xiaodemos
 * @date: 2025-04-05 18:38
 * @description: 基本工具类
 */


public class BaseUtils {

    // 生成一个带时间戳的地震编码
    public static String generationCode(LocalDateTime time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = time.format(formatter);
        String code = "T" + timestamp + BaseConstants.XI_AN_AREA_CODE;

        return code;
    }

    // 生成一个带时间戳的暴雨编码
    public static String generationRainCode(LocalDateTime time) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = time.format(formatter);
        String code = "R" + timestamp + BaseConstants.XI_AN_AREA_CODE;

        return code;
    }



}
