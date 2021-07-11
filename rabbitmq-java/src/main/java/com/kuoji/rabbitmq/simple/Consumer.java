package com.kuoji.rabbitmq.simple;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class Consumer {

    public static void main(String[] args) {

        // 1: 创建连接工程
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");

        Connection connection = null;
        Channel channel = null;
        try {
            // 2: 创建连接Connection
            connection = connectionFactory.newConnection("生产者");
            // 3: 通过连接获取通道Channel
            channel = connection.createChannel();

            // 4: 通过创建交换机,声明队列,绑定关系,路由Key,发送消息和接收消息
            String queueName = "queue1";
            channel.basicConsume(queueName, true,
                    (s, delivery) -> System.out.println("收到的消息是" + new String(delivery.getBody(), StandardCharsets.UTF_8)),
                    s -> System.out.println("接收失败了")
            );
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            // 7: 关闭连接
            if (channel != null && channel.isOpen()){
                try{
                    channel.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            // 8: 关闭通道
            if (connection != null && connection.isOpen()){
                try{
                    connection.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

}
