package com.kuoji.orderrabbitmqcomsumer.service.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = {"sms.fanout.queue"})
public class FanoutSMSConsumer {

    @RabbitHandler
    private void reviceMessage(String message){
        System.out.println("sms fanout ---接收到了订单信息是 : ->" + message);
    }

}
