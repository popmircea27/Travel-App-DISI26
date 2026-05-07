package com.example.travelappbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TravelAppBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelAppBeApplication.class, args);
    }

}
