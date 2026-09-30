package com.bloodbond;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BloodBondApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodBondApplication.class, args);
    }
}
