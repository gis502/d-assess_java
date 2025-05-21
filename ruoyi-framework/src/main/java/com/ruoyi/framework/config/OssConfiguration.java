package com.ruoyi.framework.config;

import com.ruoyi.common.properties.QiniuOssProperties;
import com.ruoyi.common.utils.QiniuOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    // @ConditionalOnBean // 当没有这个bean的时候才创建
    public QiniuOssUtil qiniuOssUtil(QiniuOssProperties qiniuOssProperties) {
        log.info("开始创建七牛云OSS工具类对象: {}", qiniuOssProperties);
        return new QiniuOssUtil(
                qiniuOssProperties.getAccessKey(),
                qiniuOssProperties.getSecretKey(),
                qiniuOssProperties.getBucket(),
                qiniuOssProperties.getUrl()
        );
    }


}
