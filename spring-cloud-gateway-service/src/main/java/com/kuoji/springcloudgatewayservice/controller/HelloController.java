package com.kuoji.springcloudgatewayservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: kuoji
 * @Date: 2021/09/22/18:40
 * @Description:
 */
@RestController
public class HelloController {

    @GetMapping("/say")
    public String sayHello(){
        return "say Hello!";
    }

}
