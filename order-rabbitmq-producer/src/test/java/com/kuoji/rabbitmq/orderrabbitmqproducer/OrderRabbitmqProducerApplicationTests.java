package com.kuoji.rabbitmq.orderrabbitmqproducer;

import com.kuoji.rabbitmq.orderrabbitmqproducer.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@SpringBootTest
public class OrderRabbitmqProducerApplicationTests {

    @Autowired
    private OrderService orderService;

    @Test
    void contextLoads(){
        orderService.makeOrder("1","1",12);
    }

    @Test
    void testOrderDirect(){
        orderService.makeOrderDirect("1","1",12);
    }

    @Test
    void testOrderTopic(){
        orderService.makeOrderTopic("1","1",12);
    }

    @Test
    void testOrderTtl(){
        orderService.makeOrderTtl("1","1",12);
    }

    @Test
    void testOrderTtlMessage(){
        orderService.makeOrderTtlMessage("1","1",12);
    }




}
