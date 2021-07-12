package com.kuoji.rabbitmq.orderrabbitmqproducer;

import com.kuoji.rabbitmq.orderrabbitmqproducer.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderRabbitmqProducerApplicationTests {

    @Autowired
    private OrderService orderService;

    @Test
    void contextLoads(){
        orderService.makeOrder("1","1",12);
    }


}
