package com.emmes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emmes.entity.WxUserInfo;
import com.emmes.mapper.WxUserInfoMapper;
import com.emmes.service.IWxUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 微信用户信息Service实现类
 */
@Service("wxUserInfoService")
public class IWxUserInfoServiceImpl extends ServiceImpl<WxUserInfoMapper,WxUserInfo> implements IWxUserInfoService {

    @Autowired
    private WxUserInfoMapper wxUserInfoMapper;


}
