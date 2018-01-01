package com.trading.forex;

import com.trading.forex.service.InstrumentService;
import com.trading.forex.service.impl.InstrumentServiceDBImpl;
import com.trading.forex.service.impl.InstrumentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.trading.forex", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {InstrumentServiceDBImpl.class})})
@EnableFeignClients
@EnableScheduling
@EnableRetry
@Slf4j
public class RobotApp {

    public static void main(String[] args) {


        SpringApplication.run(RobotApp.class, args);
    }


    @Bean
    public InstrumentService instrumentService() {
        return new InstrumentServiceImpl();
    }
}
