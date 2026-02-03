package com.ruoyi.system.core.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * @author: xiaodemos
 * @date: 2025-04-11 16:28
 * @description: rabbitmq确认消息回调函数配置
 */

@Slf4j
@Configuration
public class RabbitConfig {
/*

    // 设置队列名称
    public final static String THEMATIC_MAP = "thematic.map";   // 专题图队列
    public final static String DISASTER_REPORT = "disaster.report";     // 灾情报告队列
    public final static String SEISMIC_AFFECTED = "seismic.affected";   // 地震影响场队列


    public final static String RAIN_MAP = "rain.map";
    public final static String RAIN_REPORT = "rain.report";


    // 定义交换机名称
    public final static String DISASTER_EXCHANGE = "disasterAssessment";

    // 定义队列
    @Bean
    public Queue thematicMapQueue() {
        return new Queue(THEMATIC_MAP, true);
    }

    @Bean
    public Queue disasterReportQueue() {
        return new Queue(DISASTER_REPORT, true);
    }

    @Bean
    public Queue seismicAffectedQueue() {
        return new Queue(SEISMIC_AFFECTED, true);
    }

    @Bean
    public Queue rainMapQueue() {
        return new Queue(RAIN_MAP, true);
    }

    @Bean
    public Queue rainReportQueue() {
        return new Queue(RAIN_REPORT, true);
    }

    // 定义交换机
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(DISASTER_EXCHANGE);
    }


    // 将 thematicMapQueue 队列和 disasterAssessment 交换机绑定,而且绑定的键值为thematic.map
    // 这样只要是消息携带的路由键是thematic.map,才会分发到该队列
    @Bean
    public Binding bindingExchangeMessageOfMap() {
        return BindingBuilder.bind(thematicMapQueue()).to(exchange()).with(THEMATIC_MAP);
    }

    @Bean
    public Binding bindingExchangeMessageOfReport() {
        return BindingBuilder.bind(disasterReportQueue()).to(exchange()).with(DISASTER_REPORT);
    }

    @Bean
    public Binding bindingExchangeMessageOfAffected() {
        return BindingBuilder.bind(seismicAffectedQueue()).to(exchange()).with(SEISMIC_AFFECTED);
    }

    @Bean
    public Binding bindingExchangeMessageOfRainMap() {
        return BindingBuilder.bind(rainMapQueue()).to(exchange()).with(RAIN_MAP);
    }

    @Bean
    public Binding bindingExchangeMessageOfRainReport() {
        return BindingBuilder.bind(rainReportQueue()).to(exchange()).with(RAIN_REPORT);
    }

    // 设置消息回调函数 自动确认消息 ack
    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        //设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("confirm call back：correlationData-{}，ack-{}，cause-{}" ,correlationData, ack, cause);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("return call back：message-{}，replyCode-{}，replyText-{}，exchange-{}，routingKey-{}" + message, replyCode, replyText, exchange, routingKey);
            }
        });

        return rabbitTemplate;
    }
*/

    // 设置队列名称
    public final static String THEMATIC_MAP = "thematic.map";   // 专题图队列
    public final static String DISASTER_REPORT = "disaster.report";     // 灾情报告队列
    public final static String SEISMIC_AFFECTED = "seismic.affected";   // 地震影响场队列
    public final static String RAIN_MAP = "rain.map";
    public final static String RAIN_REPORT = "rain.report";

    // 定义交换机名称
    public final static String DISASTER_EXCHANGE = "disasterAssessment";

    // 定义队列（均为持久化队列）
    @Bean
    public Queue thematicMapQueue() {
        return new Queue(THEMATIC_MAP, true);
    }

    @Bean
    public Queue disasterReportQueue() {
        return new Queue(DISASTER_REPORT, true);
    }

    @Bean
    public Queue seismicAffectedQueue() {
        return new Queue(SEISMIC_AFFECTED, true);
    }

    @Bean
    public Queue rainMapQueue() {
        return new Queue(RAIN_MAP, true);
    }

    @Bean
    public Queue rainReportQueue() {
        return new Queue(RAIN_REPORT, true);
    }

    // 定义Topic交换机（持久化）
    @Bean
    public TopicExchange exchange() {
        // 补充交换机持久化配置（默认true，但显式声明更规范）
        return new TopicExchange(DISASTER_EXCHANGE, true, false);
    }

    // 队列与交换机绑定
    @Bean
    public Binding bindingExchangeMessageOfMap() {
        return BindingBuilder.bind(thematicMapQueue()).to(exchange()).with(THEMATIC_MAP);
    }

    @Bean
    public Binding bindingExchangeMessageOfReport() {
        return BindingBuilder.bind(disasterReportQueue()).to(exchange()).with(DISASTER_REPORT);
    }

    @Bean
    public Binding bindingExchangeMessageOfAffected() {
        return BindingBuilder.bind(seismicAffectedQueue()).to(exchange()).with(SEISMIC_AFFECTED);
    }

    @Bean
    public Binding bindingExchangeMessageOfRainMap() {
        return BindingBuilder.bind(rainMapQueue()).to(exchange()).with(RAIN_MAP);
    }

    @Bean
    public Binding bindingExchangeMessageOfRainReport() {
        return BindingBuilder.bind(rainReportQueue()).to(exchange()).with(RAIN_REPORT);
    }

    // ========== 修复1：生产端RabbitTemplate配置（移除错误的消费端参数） ==========
    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConnectionFactory(connectionFactory);
        // 设置开启Mandatory，触发ReturnCallback（消息路由失败时回调）
        rabbitTemplate.setMandatory(true);

        // 消息确认回调（生产端→Broker）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("confirm call back：correlationData-{}，ack-{}，cause-{}", correlationData, ack, cause);
            // 补充：ack=false时记录错误日志，便于排查消息发送失败
            if (!ack) {
                log.error("消息发送到Broker失败！cause:{}", cause);
            }
        });

        // 修复：日志字符串拼接错误，补充路由失败的详细日志
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.info("return call back：message-{}，replyCode-{}，replyText-{}，exchange-{}，routingKey-{}",
                    new String(message.getBody()), replyCode, replyText, exchange, routingKey);
            log.error("消息路由失败！exchange:{}，routingKey:{}，原因:{}", exchange, routingKey, replyText);
        });

        return rabbitTemplate;
    }

    // ========== 新增：消费端容器工厂配置（原错误配在RabbitTemplate的参数移到这里） ==========
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 消费端核心线程数（匹配32核服务器）
        factory.setConcurrentConsumers(10);
        // 消费端最大线程数
        factory.setMaxConcurrentConsumers(32);
        // 手动确认模式（避免自动确认导致消费异常时消息丢失）
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 补充：每次从队列获取的消息数（预取数），避免单个消费线程抢占过多消息
        factory.setPrefetchCount(5);
        // 补充：消费异常时的重试（可选，根据业务调整）
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    // ========== 新增：消费端重试配置（可选，防止单次消费异常导致消息丢失） ==========
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        RetryTemplate retryTemplate = new RetryTemplate();
        // 重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        // 每次重试间隔1秒
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000);
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .build();
    }
}
