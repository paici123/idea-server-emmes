package com.emmes.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emmes.entity.Order;
import com.emmes.entity.OrderDetail;
import com.emmes.entity.R;
import com.emmes.properties.WeixinpayProperties;
import com.emmes.service.IOrderDetailService;
import com.emmes.service.IOrderService;
import com.emmes.util.*;
import io.jsonwebtoken.Claims;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.beans.Transient;
import java.security.MessageDigest;
import java.util.*;

/**
 * 订单Controller控制器
 */
@RestController
@RequestMapping("/my/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private WeixinpayProperties weixinpayProperties;

    /**
     * 创建订单，返回订单号
     * @return
     */
    @RequestMapping("/create")
    @Transactional
    public R create(@RequestBody Order order,@RequestHeader(value="token") String token){
        //通过token获取openid
        System.out.println("token:"+token);
        //添加订单到数据库
        Claims claims = JwtUtils.validateJWT(token).getClaims();
        if(claims!=null){
            System.out.println("openid="+claims.getId());
            order.setUserId(claims.getId());
        }
        order.setOrderNo("JAVA"+DateUtil.getCurrentDateStr());//订单号
        order.setCreateDate(new Date());//创建日期
        System.out.println(new Date());
        OrderDetail[] goods = order.getGoods();//商品详情
        orderService.save(order);//添加订单到数据库
        //遍历添加订单详情到数据库
        for(int i=0;i<goods.length;i++){
            OrderDetail orderDetail=goods[i];
            orderDetail.setMId(order.getId());
            orderDetailService.save(orderDetail);
        }
        Map<String,Object> resultMap=new HashMap<String,Object>();
        resultMap.put("orderNo",order.getOrderNo());
        return R.ok(resultMap);
    }

    /**
     * 调用统一下单，预付款
     */
    @RequestMapping("/preparePay")
    public R preparePay(@RequestBody String orderNo)throws Exception{
        System.out.println("orderNo="+orderNo);
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("orderNo", orderNo));
        /**
        System.out.println("total_fee="+order.getTotalPrice().movePointRight(2));
        System.out.println("========");
        System.out.println("appid="+weixinpayProperties.getAppid());
        System.out.println("mch_id="+weixinpayProperties.getMch_id());
        System.out.println("openid="+order.getUserId());
        System.out.println("nonce_str="+StringUtil.getRandomString(32));
        System.out.println("body="+"java1234mall商品购买测试");
        System.out.println("out_trade_no="+orderNo);
        System.out.println("spbill_create_ip="+"127.0.0.1");
        System.out.println("notify_url="+weixinpayProperties.getNotify_url());
        System.out.println("trade_type="+"JSAPI");
        System.out.println("sign=");
        */
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("appid",weixinpayProperties.getAppid());
        map.put("mch_id",weixinpayProperties.getMch_id());
        map.put("openid",order.getUserId());
        map.put("nonce_str",StringUtil.getRandomString(32));

        map.put("body","商品购买测试");
        map.put("total_fee",order.getTotalPrice().movePointRight(2));
        map.put("out_trade_no",orderNo);
        map.put("spbill_create_ip","127.0.0.1");
        map.put("notify_url",weixinpayProperties.getNotify_url());
        map.put("trade_type","JSAPI");
        map.put("sign",getSign(map));
        // 参数转成xml
        String xml=XmlUtil.genXml(map);
        System.out.println("xml="+xml);
        HttpResponse httpResponse = HttpClientUtil.sendXMLDataByPost(weixinpayProperties.getUrl().toString(), xml);
        String httpEntityContent = HttpClientUtil.getHttpEntityContent(httpResponse);
        System.out.println("httpEntityContent="+httpEntityContent);
        Map resultMap = XmlUtil.doXMLParse(httpEntityContent);
        System.out.println("resultMap="+resultMap);
        if(resultMap.get("result_code").equals("SUCCESS")){
            Map<String,Object>payMap=new HashMap<>();
            payMap.put("appId",resultMap.get("appid"));
            payMap.put("timeStamp",System.currentTimeMillis()/1000+"");
            payMap.put("nonceStr",StringUtil.getRandomString(32));
            payMap.put("package","prepay_id="+resultMap.get("prepay_id"));
            payMap.put("signType","MD5");
            payMap.put("paySign",getSign(payMap));
            payMap.put("orderNo",orderNo);
            return R.ok(payMap);
        }else{
            return R.error(500,"系统报错，请联系管理员");
        }
    }

    /**
     * 微信支付签名算法sign
     */
    private String getSign(Map<String,Object> map) {
        StringBuffer sb = new StringBuffer();
        String[] keyArr = (String[]) map.keySet().toArray(new String[map.keySet().size()]);//获取map中的key转为array
        Arrays.sort(keyArr);//对array排序
        for (int i = 0, size = keyArr.length; i < size; ++i) {
            if ("sign".equals(keyArr[i])) {
                continue;
            }
            sb.append(keyArr[i] + "=" + map.get(keyArr[i]) + "&");
        }
        sb.append("key=" + weixinpayProperties.getKey());
        String sign = string2MD5(sb.toString());
        return sign;
    }

    /***
     * MD5加码 生成32位md5码
     */
    private String string2MD5(String str){
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 订单查询  type 值 0 全部订单  1 待付款   2  待收货  3 退款/退货
     * @return
     */
    @RequestMapping("/list")
    public R list(Integer type,Integer page,Integer pageSize,@RequestHeader(value="token") String token){
        System.out.println(type+"."+page+"."+pageSize);
        System.out.println("token:"+token);
        if(StringUtil.isNotEmpty(token)){
            Claims claims = JwtUtils.validateJWT(token).getClaims();
            if(claims!=null){
                String openId = claims.getId();

                System.out.println(openId);
            }else{
                return R.error(500,"鉴权失败！");
            }
        }else{
            return R.error(500,"无权限访问！");
        }
        List<Order> orderList=null;
        Page<Order> pageOrder=new Page<>(page,pageSize); // 定义分页
        Map<String,Object> resultMap=new HashMap<String,Object>();

        if(type==0){  // 查询全部
            // orderList = orderService.list();
            Page<Order> orderReslut = orderService.page(pageOrder);
            System.out.println("总记录："+orderReslut.getTotal());
            System.out.println("总页数："+orderReslut.getPages());
            System.out.println("当前页数据："+orderReslut.getRecords());
            orderList=orderReslut.getRecords();
            resultMap.put("total",orderReslut.getTotal());
            resultMap.put("totalPage",+orderReslut.getPages());

        }else{  // 根据状态查询
            // orderList=orderService.list(new QueryWrapper<Order>().eq("status",type));
            Page<Order> orderReslut =orderService.page(pageOrder,new QueryWrapper<Order>().eq("status",type));
            System.out.println("总记录："+orderReslut.getTotal());
            System.out.println("总页数："+orderReslut.getPages());
            System.out.println("当前页数据："+orderReslut.getRecords());
            orderList=orderReslut.getRecords();
            resultMap.put("total",orderReslut.getTotal());
            resultMap.put("totalPage",+orderReslut.getPages());
        }
        resultMap.put("page",page);
        resultMap.put("orderList",orderList);
        return R.ok(resultMap);
    }

}
