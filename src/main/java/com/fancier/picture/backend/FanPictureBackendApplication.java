package com.fancier.picture.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("com.fancier.picture.backend.mapper")
public class FanPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FanPictureBackendApplication.class, args);
    }

}
