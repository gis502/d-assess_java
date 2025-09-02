package com.ruoyi.system.core.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author: xiaodemos
 * @date: 2025-04-11 16:28
 * @description: rabbitmq确认消息回调函数配置
 */

@Slf4j
@Configuration
public class RabbitConfig {

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
        return BindingBuilder.bind(seismicAffectedQueue()).to(exchange()).with(DISASTER_REPORT);
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

}
