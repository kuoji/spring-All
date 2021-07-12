package com.kuoji.rabbitmq.orderrabbitmqproducer.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 模拟用户下单
     * @param userId
     * @param productId
     * @param num
     */
    public void makeOrder(String userId, String productId, int num){

        // 1: 根据商品id查询库存是否充足

        // 2: 保存订单
        String orderId = UUID.randomUUID().toString();
        System.out.println("订单生成成功: " + orderId);

        // 3: 通过MQ来完成消息的分发
        // 交换机   路由key/queue队列名称   消息内容
        String exchangeName = "fanout_order_exchange";
        String routeKey = "";
        rabbitTemplate.convertAndSend(exchangeName,routeKey,orderId);

    }

    public void makeOrderDirect(String userId, String productId, int num){

        // 1: 根据商品id查询库存是否充足

        // 2: 保存订单
        String orderId = UUID.randomUUID().toString();
        System.out.println("订单生成成功: " + orderId);

        // 3: 通过MQ来完成消息的分发
        // 交换机   路由key/queue队列名称   消息内容
        String exchangeName = "direct_order_exchange";
        String routeKey = "";
        rabbitTemplate.convertAndSend(exchangeName,"email",orderId);
        rabbitTemplate.convertAndSend(exchangeName,"duanxin",orderId);

    }

    public void makeOrderTopic(String userId, String productId, int num){

        // 1: 根据商品id查询库存是否充足

        // 2: 保存订单
        String orderId = UUID.randomUUID().toString();
        System.out.println("订单生成成功: " + orderId);

        // 3: 通过MQ来完成消息的分发
        // 交换机   路由key/queue队列名称   消息内容
        String exchangeName = "topic_order_exchange";
        // #.duanxin.#
        // *.email.#
        // com.#
        String routeKey = "com.duanxin";
        rabbitTemplate.convertAndSend(exchangeName,routeKey,orderId);

    }
}
