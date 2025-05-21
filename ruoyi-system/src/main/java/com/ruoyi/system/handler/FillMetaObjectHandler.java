package com.ruoyi.system.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
* @author: xiaodemos
* @date: 2025-04-04 10:22
* @description: 字段自动填充处理器
*/
@Slf4j
@Component
public class FillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        // this.strictInsertFill(metaObject, "createUserId", Long.class, 123456L);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        // this.strictInsertFill(metaObject, "updateUserId", Long.class, 123456L);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

}
