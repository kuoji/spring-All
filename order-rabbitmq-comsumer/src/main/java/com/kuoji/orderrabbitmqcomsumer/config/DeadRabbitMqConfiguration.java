package com.kuoji.orderrabbitmqcomsumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kuoji
 */
@Configuration
public class DeadRabbitMqConfiguration {

    // 1: 声明注册 Direct 模式的交换机
    @Bean
    public DirectExchange deadDirectExchange(){
        return new DirectExchange("dead_direct_exchange",true,false);
    }

    // 队列的过期时间
    @Bean
    public Queue deadDirectQueue(){
        return new Queue("dead.direct.queue",true);
    }

    @Bean
    public Binding deadDirectBingding(){
        return BindingBuilder.bind(deadDirectQueue()).to(deadDirectExchange()).with("dead");
    }

}
