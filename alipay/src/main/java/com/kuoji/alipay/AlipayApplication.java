package com.kuoji.alipay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kuoji.alipay.mapper")
public class AlipayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlipayApplication.class, args);
    }

}
