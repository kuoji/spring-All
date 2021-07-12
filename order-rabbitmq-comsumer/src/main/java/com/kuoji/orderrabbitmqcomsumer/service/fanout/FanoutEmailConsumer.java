package com.kuoji.orderrabbitmqcomsumer.service.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RabbitListener(queues = {"email.fanout.queue"})
public class FanoutEmailConsumer {

    @RabbitHandler
    private void reviceMessage(String message){
        System.out.println("email fanout ---接收到了订单信息是 : ->" + message);
    }

}
