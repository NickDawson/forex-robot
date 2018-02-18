package com.trading.forex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.trading.forex", "org.springframework.security.core.userdetails"})
@EnableFeignClients
@EnableScheduling
@EnableRetry
@Slf4j
public class RobotApp {

    public static void main(String[] args) {

        SpringApplication.run(RobotApp.class, args);
    }
}
