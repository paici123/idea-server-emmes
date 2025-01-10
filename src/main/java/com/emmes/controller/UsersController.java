package com.emmes.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.emmes.constant.SystemConstant;
import com.emmes.entity.R;
import com.emmes.entity.WxUserInfo;
import com.emmes.properties.WeixinProperties;
import com.emmes.service.IWxUserInfoService;
import com.emmes.util.HttpClientUtil;
import com.emmes.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信用户Controller
 */
@RestController
@RequestMapping("/user")
public class  UsersController {

    @Autowired
    private WeixinProperties weixinProperties;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private IWxUserInfoService wxUserInfoService;

    /**
     * 微信登录
     * @return
     */
    @RequestMapping("/wxlogin")
    public R wxLogin(@RequestBody WxUserInfo wxUserInfo){
        //System.out.println(wxUserInfo.getCode());//打印从前端获取到等wx用登录信息
        String jscode2sessionUrl=weixinProperties.getJscode2sessionUrl()+"?appid="+weixinProperties.getAppid()+"&secret="+weixinProperties.getSecret()+"&js_code="+wxUserInfo.getCode()+"&grant_type=authorization_code";
        //System.out.println(jscode2sessionUrl);
        String result = httpClientUtil.sendHttpGet(jscode2sessionUrl); // 带code请求获取openId
        //System.out.println(result);
        JSONObject jsonObject = JSON.parseObject(result);//解析openId
        String openid = jsonObject.get("openid").toString(); // 获取openId
        //System.out.println(openid);
        //插入到数据库  假如是用户不存在  我们插入到数据库  如果用户存在我们跟新用户
        WxUserInfo resultUserInfo = wxUserInfoService.getOne(new QueryWrapper<WxUserInfo>().eq("openid", openid));
        if(resultUserInfo==null){ // 不存在 插入用户
            System.out.println("不存在，插入用户信息");
            wxUserInfo.setOpenid(openid);
            wxUserInfo.setRegisterDate(new Date());
            wxUserInfo.setLastLoginDate(new Date());
            wxUserInfoService.save(wxUserInfo);
            //System.out.println(wxUserInfo.getId());
        }else{  // 存在 更新用户信息
            System.out.println("存在 更新用户");
            resultUserInfo.setNickName(wxUserInfo.getNickName());
            resultUserInfo.setAvatarUrl(wxUserInfo.getAvatarUrl());
            resultUserInfo.setLastLoginDate(new Date());
            wxUserInfoService.updateById(resultUserInfo);
//            wxUserInfo.setId(resultUserInfo.getId());
        }//利用jwt生成token返回给客户端(前端)
        String token = JwtUtils.createJWT(openid, wxUserInfo.getNickName(), SystemConstant.JWT_TTL);
        Map<String,Object> resultMap=new HashMap<String,Object>();
        resultMap.put("token",token);//返回的token为空，也就是wx登录的用户信息  49集
        return R.ok(resultMap);

    }

}
