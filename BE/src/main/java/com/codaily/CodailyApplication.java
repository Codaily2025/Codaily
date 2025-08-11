package com.codaily;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodailyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodailyApplication.class, args);
    }

}
