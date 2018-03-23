package com.fly.api;

/**
 * Created by jinxiaofei.
 * Time 2018/3/22 下午3:34
 * Desc 文件描述
 */
public interface DemoService {

    default String sayHello(String name) {
        throw new UnsupportedOperationException("there is no implenments");
    }
}
