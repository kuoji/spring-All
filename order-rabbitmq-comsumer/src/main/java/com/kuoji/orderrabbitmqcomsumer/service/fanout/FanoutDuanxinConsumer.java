package com.kuoji.orderrabbitmqcomsumer.service.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = {"duanxin.fanout.queue"})
public class FanoutDuanxinConsumer {

    @RabbitHandler
    private void reviceMessage(String message){
        System.out.println("duanxin fanout ---接收到了订单信息是 : ->" + message);
    }


}
