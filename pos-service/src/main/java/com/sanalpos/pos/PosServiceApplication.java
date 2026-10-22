package com.sanalpos.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class PosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosServiceApplication.class, args);
    }
}