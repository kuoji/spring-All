package com.kuoji.alipay.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("kss_courses")
public class ProductCourse {
    // 课程ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long courseid;
    // 课程标题
    private String title;
    // 课程介绍
    private String intro;
    // 课程封面
    private String img;
    // 课程价格
    private BigDecimal price;
    // 课程状态
    private Integer status;
    // 课程创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    // 课程更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
