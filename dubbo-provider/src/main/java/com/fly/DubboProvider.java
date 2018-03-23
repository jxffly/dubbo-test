package com.fly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * Hello world!
 */
@SpringBootApplication
@Slf4j
@ImportResource("classpath:spring-dubbo.xml")
public class DubboProvider {

    private static String ENV = "spring.profiles.active";

    private static String DEFAULT_PROFILE = "qa";


    public static void main(String[] args) {

        //SpringApplication.run(AccountServiceApplication.class,args);
        SpringApplication springApplication = new SpringApplication(DubboProvider.class);
        String profile = System.getProperty(ENV);
        if (StringUtils.isEmpty(profile)) {
            log.warn("can not get any env setting,just use default:{}", DEFAULT_PROFILE);
            springApplication.setAdditionalProfiles(DEFAULT_PROFILE);
        }
        springApplication.run(args);
    }

}