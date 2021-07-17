package com.kuoji.alipay.service.order;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuoji.alipay.entity.OrderDetail;
import com.kuoji.alipay.mapper.OrderDetailMapper;
import org.springframework.stereotype.Service;



@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
