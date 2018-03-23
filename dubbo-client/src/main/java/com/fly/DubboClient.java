package com.fly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import lombok.extern.slf4j.Slf4j;


/**
 * Hello world!
 */
@SpringBootApplication
@ImportResource("classpath:spring-dubbo.xml")
@Slf4j
public class DubboClient {


    public static void main(String[] args) {

        SpringApplication.run(DubboClient.class, args);

    }

}