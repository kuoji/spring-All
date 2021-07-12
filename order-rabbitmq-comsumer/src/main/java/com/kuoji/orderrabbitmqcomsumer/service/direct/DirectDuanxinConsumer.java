package com.kuoji.orderrabbitmqcomsumer.service.direct;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = {"duanxin.direct.queue"})
public class DirectDuanxinConsumer {

    @RabbitHandler
    private void reviceMessage(String message){
        System.out.println("duanxin direct ---接收到了订单信息是 : ->" + message);
    }


}
