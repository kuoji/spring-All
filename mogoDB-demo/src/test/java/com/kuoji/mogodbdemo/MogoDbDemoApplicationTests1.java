package com.kuoji.mogodbdemo;

import com.kuoji.mogodbdemo.entity.User;
import com.kuoji.mogodbdemo.repository.UserRepository;
import com.mongodb.client.result.DeleteResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.regex.Pattern;

@SpringBootTest
class MogoDbDemoApplicationTests1 {

    // 注入mongoTemplate
    @Autowired
    private UserRepository userRepository;

    // 添加操作
    @Test
    public void create(){
        User user = new User();
        user.setAge(12);
        user.setName("Bob");
        user.setEmail("123@qq.com");
        User u = userRepository.save(user);
        System.out.println(u);
    }

    // 查询表所有记录
    @Test
    public void findAll(){
        List<User> all = userRepository.findAll();
        System.out.println(all);
    }

    // id查询
    @Test
    public void findId(){
        User user = userRepository.findById("61655d49e4f36a0b80e6993e").get();
        System.out.println(user);
    }

    // 条件查询
    @Test
    public void findUserList(){
        User user = new User();
        user.setAge(12);
        Example<User> userExample = Example.of(user);
        List<User> all = userRepository.findAll(userExample);
        System.out.println(all);
    }

    // 模糊查询
    @Test
    public void findLikeUserList(){
        // 设置模糊查询匹配规则
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        User user = new User();
        user.setAge(12);
        user.setName("B");
        Example<User> userExample = Example.of(user,matcher);
        List<User> all = userRepository.findAll(userExample);

        System.out.println(all);
    }

    // 分页查询
    @Test
    public void findPageUserList(){
        // 设置分页参数
        // 0代表第一页
        Pageable pageable = PageRequest.of(0,3);

        User user = new User();
        user.setAge(12);
        Example<User> userExample = Example.of(user);
        Page<User> page = userRepository.findAll(userExample, pageable);

        System.out.println(page);
    }

    // 修改
    @Test
    public void updateUser(){
        User user = userRepository.findById("616667084bafc1271333576a").get();
        user.setAge(22);
        user.setName("Bobbb");
        User save = userRepository.save(user);
        System.out.println(save);
    }

    // 删除
    @Test
    public void deleteUser(){
    }

}
