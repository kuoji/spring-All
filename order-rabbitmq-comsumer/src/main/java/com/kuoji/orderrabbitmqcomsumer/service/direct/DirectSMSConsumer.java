package com.kuoji.orderrabbitmqcomsumer.service.direct;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = {"sms.direct.queue"})
public class DirectSMSConsumer {

    @RabbitHandler
    private void reviceMessage(String message){
        System.out.println("sms direct ---接收到了订单信息是 : ->" + message);
    }

}
