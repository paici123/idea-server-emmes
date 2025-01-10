package com.emmes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emmes.entity.OrderDetail;
import com.emmes.mapper.OrderDetailMapper;
import com.emmes.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 订单细表Service实现类
 */
@Service("orderDetailService")
public class IOrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper,OrderDetail> implements IOrderDetailService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

}
