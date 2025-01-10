package com.emmes.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emmes.entity.Product;
import com.emmes.entity.ProductSwiperImage;
import com.emmes.entity.R;
import com.emmes.service.IProductService;
import com.emmes.service.IProductSwiperImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品控制器：主要通过请求url路径处理商品的json数据
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private IProductSwiperImageService productSwiperImageService;

    /**
     * 查询轮播商品
     * @return
     */
    @RequestMapping("/findSwiper")
    public R findSwiper(){
        List<Product> swiperProductList=productService.list(new
                QueryWrapper<Product>().eq("isSwiper",true).orderByAsc("swiperSort"));
        Map<String,Object> map=new HashMap<>();
        map.put("message",swiperProductList);
        return R.ok(map);
    }
    /**
     * 查询热卖推荐商品前8个
     **/
    @GetMapping("/findHot")
    public R findHot(){
        Page<Product> page = productService.page(new Page<>(0,8), new
                QueryWrapper<Product>().eq("isHot", true).orderByAsc("hotDateTime"));
        List<Product> hotProductList = page.getRecords();
        Map<String,Object> map=new HashMap<>();
        map.put("message",hotProductList);
        return R.ok(map);
    }
    /**
     * 根据id查询商品信息
     * */
    @GetMapping("/detail")
    public R detail(Integer id){
        Product product = productService.getById(id);
        List<ProductSwiperImage>productSwiperImageList=productSwiperImageService.list(new
                QueryWrapper<ProductSwiperImage>().eq("productId",product.getId()));
        product.setProductSwiperImageList(productSwiperImageList);
        Map<String,Object>map=new HashMap<>();
        map.put("message",product);
        return R.ok(map);
    }
    /**
     * 商品搜索
     */
    @GetMapping("/search")
    public R search(String q){
        List<Product> productList = productService.list(new
                QueryWrapper<Product>().like("name", q));
        Map<String,Object> map=new HashMap<>();
        map.put("productList",productList);
        return R.ok(map);
    }

}
