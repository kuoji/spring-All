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
public class TTLRabbitMqConfiguration {

    // 1: 声明注册 Direct 模式的交换机
    @Bean
    public DirectExchange ttlDirectExchange(){
        return new DirectExchange("ttl_direct_exchange",true,false);
    }

    // 队列的过期时间
    @Bean
    public Queue ttlDirectQueue(){
        // 修改 args 队列的参数，要先删除队列再创建 (线上模式则重新创建，切记不能删除正在运行的队列)
        Map<String,Object> args = new HashMap<>();
        // 设置过期时间，这里一定是 int 单位为 ms
        args.put("x-message-ttl",5000);
        args.put("x-max-length",5);
//        // 设置死信队列
        args.put("x-dead-letter-exchange","dead_direct_exchange");
        // 这里为 direct 模式 则有 key,  fanout不需要配置
        args.put("x-dead-letter-routing-key","dead");
        return new Queue("ttl.direct.queue",true,false,false,args);
    }

    // 普通队列，在service给消息设置过期时间
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
