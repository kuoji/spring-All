package com.kuoji.mogodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @Author: kuoji
 * @Date: 2021/10/12/17:57
 * @Description:
 */

@Data
@Document("User")
public class User {
    @Id
    private String id;

    private String name;

    private Integer age;

    private String email;

    private String createDate;
}
