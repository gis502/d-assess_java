package com.ruoyi.common.exception;

/**
 * @author: xiaodemos
 * @date: 2025-04-17 4:15
 * @description: 图件下载失败
 */


public class DownLoadException extends RuntimeException {

    public DownLoadException(String message) {
        super(message);
    }

}
