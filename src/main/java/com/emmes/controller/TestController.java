package com.emmes.controller;

import com.emmes.entity.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emmes")
public class TestController {//测试
    @GetMapping("/test")
    public R test(){
        return R.ok("流弊Java123");
    }
}
