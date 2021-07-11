package com.kuoji.rabbitmq.simple;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 *  简单模式Simple
 */
public class Producer {

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

            /*
             * 队列的名称
             * 是否要持久化 durable=false 所谓持久化消息是否存盘
             * 排他性，是否是独占独立
             * 是否自动删除，随着最后一个消费者消息完毕，消息以后是否把队列自动删除
             * 携带附属参数
             */
            channel.queueDeclare(queueName,false,false,false,null);
            // 5: 准备消息内容
            String message = "Hello kuoji!!!";
            // 6: 发送消息给队列 queue
            channel.basicPublish("",queueName, MessageProperties.PERSISTENT_BASIC,message.getBytes());

            System.out.println("消息发送成功！！！");
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
