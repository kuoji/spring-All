package com.kuoji.mogodbdemo.repository;


import com.kuoji.mogodbdemo.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author: kuoji
 * @Date: 2021/10/13/12:52
 * @Description:
 */

public interface UserRepository extends MongoRepository<User,String> {

}
