package com.kuoji.orderrabbitmqcomsumer.service.topic;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "sms.topic.queue", durable = "true", autoDelete = "false"),
        exchange = @Exchange(value = "topic_order_exchange",type = ExchangeTypes.TOPIC),
        key = "com.#"
))
public class TopicSMSConsumer {

    @RabbitHandler
    private void reviceMessage(String message){
        System.out.println("sms topic ---接收到了订单信息是 : ->" + message);
    }

}
