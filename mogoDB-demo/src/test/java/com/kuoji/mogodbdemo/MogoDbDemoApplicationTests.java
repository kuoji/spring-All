package com.kuoji.mogodbdemo;

import com.kuoji.mogodbdemo.entity.User;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.regex.Pattern;

@SpringBootTest
class MogoDbDemoApplicationTests {

    // 注入mongoTemplate
    @Autowired
    private MongoTemplate mongoTemplate;

    // 添加操作
    @Test
    public void create(){
        User user = new User();
        user.setAge(20);
        user.setName("test");
        user.setEmail("123@qq.com");
        User user1 = mongoTemplate.insert(user);

        System.out.println(user1);
    }

    // 查询表所有记录
    @Test
    public void findAll(){
        List<User> all = mongoTemplate.findAll(User.class);
        System.out.println(all);
    }

    // id查询
    @Test
    public void findId(){
        User user = mongoTemplate.findById("61655d49e4f36a0b80e6993e", User.class);
        System.out.println(user);
    }

    // 条件查询
    @Test
    public void findUserList(){
        // name = test and age = 20
        Query query = new Query(Criteria.where("name").is("test")
                                    .and("age").is(20));
        List<User> users = mongoTemplate.find(query, User.class);

        System.out.println(users);
    }

    // 模糊查询
    @Test
    public void findLikeUserList(){
        // name like test
        String name = "est";
        String regex = String.format("%s%s%s","^.*",name,".*$");
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));
        List<User> users = mongoTemplate.find(query, User.class);

        System.out.println(users);
    }

    // 分页查询
    @Test
    public void findPageUserList(int pageNo, int pageSize){
        String name = "est";
        // 条件构建
        String regex = String.format("%s%s%s","^.*",name,".*$");
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));

        // 分页构建

        // 查询记录数
        long count = mongoTemplate.count(query, User.class);

        // 分页
        List<User> users = mongoTemplate.find(query.skip((long) (pageNo - 1) * pageSize)
                                    .limit(pageSize), User.class);
    }

    // 修改
    @Test
    public void updateUser(){
        // 根据id查询
        User user = mongoTemplate.findById("61655d49e4f36a0b80e6993e", User.class);

        // 设置修改值
        user.setName("test_1");
        user.setAge(11);
        user.setEmail("111@qq.com");

        mongoTemplate.save(user);
    }

    // 删除
    @Test
    public void deleteUser(){
        Query query = new Query(Criteria.where("_id").is("61655d49e4f36a0b80e6993e"));
        DeleteResult remove = mongoTemplate.remove(query, User.class);
        long deletedCount = remove.getDeletedCount();

        System.out.println(deletedCount);
    }

}
