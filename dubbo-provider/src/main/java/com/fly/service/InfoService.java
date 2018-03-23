package com.fly.service;

import com.fly.api.DemoService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * Created by jinxiaofei.
 * Time 2018/3/22 下午5:10
 * Desc 文件描述
 */
@RestController
@RequestMapping("provider")
public class InfoService {
    @Resource
    private DemoService demoService;
    @RequestMapping("say")
    public String say(@RequestParam String name){
        return demoService.sayHello(name);
    }



}
