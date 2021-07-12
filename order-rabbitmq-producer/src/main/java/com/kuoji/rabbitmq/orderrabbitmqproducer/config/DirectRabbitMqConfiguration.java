package com.kuoji.orderrabbitmqcomsumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kuoji
 */
@Configuration
public class DirectRabbitMqConfiguration {

    // 1: 声明注册 Direct 模式的交换机
    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange("direct_order_exchange",true,false);
    }

    // 2: 声明队列 sms.Direct.queue email.Direct.queue  duanxin.Direct.queue
    @Bean
    public Queue directSmsQueue(){
        return new Queue("sms.direct.queue",true);
    }

    @Bean
    public Queue directEmailQueue(){
        return new Queue("email.direct.queue",true);
    }

    @Bean
    public Queue directDuanxinQueue(){
        return new Queue("duanxin.direct.queue",true);
    }


    // 3: 完成绑定关系
    @Bean
    public Binding smsBingDing(){
        return BindingBuilder.bind(directExchange()).to(directExchange()).with("sms");
    }

    @Bean
    public Binding emailBingDing(){
        return BindingBuilder.bind(directEmailQueue()).to(directExchange()).with("email");
    }

    @Bean
    public Binding duanxinBingDing(){
        return BindingBuilder.bind(directDuanxinQueue()).to(directExchange()).with("duanxin");
    }

}
