package com.fly.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.fly.api.DemoService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



/**
 * Created by jinxiaofei.
 * Time 2018/3/22 下午3:42
 * Desc 文件描述
 */
@RestController
@RequestMapping("/dubbo")
public class DemoConsumerController {

    @Reference(version = "1.0.0",
            application = "${dubbo.application.id}",
            url = "dubbo://localhost:12345"
            )
    private DemoService demoService;

    @RequestMapping("/sayHello")
    public String sayHello(@RequestParam String name) {
        return demoService.sayHello(name);
    }
    @RequestMapping("/hash")
    public String hash(@RequestParam String name) {
        return String.valueOf(demoService.equals(name));
    }


}
