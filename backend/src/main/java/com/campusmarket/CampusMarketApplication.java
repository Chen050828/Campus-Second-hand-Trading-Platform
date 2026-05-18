package com.campusmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusMarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusMarketApplication.class, args);
    }
}
