package com.kuoji.rabbitmq.orderrabbitmqproducer.config;

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
public class TTLRabbitMqConfiguration {

    // 1: 声明注册 Direct 模式的交换机
    @Bean
    public DirectExchange ttlDirectExchange(){
        return new DirectExchange("ttl_direct_exchange",true,false);
    }

    // 队列的过期时间
    @Bean
    public Queue ttlDirectQueue(){
        // 设置过期时间
        Map<String,Object> args = new HashMap<>();
        args.put("x-message-ttl",5000); // 这里一定是int类型
        return new Queue("ttl.direct.queue",true,false,false,args);
    }

    // 普通队列
    @Bean
    public Queue ttlDirectMessageQueue(){
        return new Queue("ttl.message.direct.queue",true);
    }


    @Bean
    public Binding ttlDirectBingding(){
        return BindingBuilder.bind(ttlDirectQueue()).to(ttlDirectExchange()).with("ttl");
    }

    @Bean
    public Binding ttlDirectMessageBingding(){
        return BindingBuilder.bind(ttlDirectMessageQueue()).to(ttlDirectExchange()).with("ttlmessage");
    }

}
