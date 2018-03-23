package com.fly.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.fly.api.DemoService;


/**
 * Created by jinxiaofei.
 * Time 2018/3/22 下午3:29
 * Desc 文件描述
 */
@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
@org.springframework.stereotype.Service
public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        return "Hello, " + name + " (from Spring Boot)";
    }
}
