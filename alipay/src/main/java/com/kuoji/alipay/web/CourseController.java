package com.kuoji.alipay.web;

import com.kuoji.alipay.entity.ProductCourse;
import com.kuoji.alipay.service.course.ProductCourseService;
import com.kuoji.alipay.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class CourseController {

    @Autowired
    private ProductCourseService productCourseService;

    /*
     * @Author 徐柯
     * @Description 查询课程产品信息
     * @Date 14:38 2021/4/1
     * @Param []
     * @return com.kuangstudy.alipay.vo.R
     **/
    @GetMapping("/api/course/list")
    @ResponseBody
    public R main() {
        List<ProductCourse> courseList = productCourseService.list();

        return R.ok().data("courseList", courseList);
    }
}
