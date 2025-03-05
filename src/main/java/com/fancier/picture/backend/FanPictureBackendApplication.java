package com.fancier.picture.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.fancier.picture.backend.mapper")
public class FanPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FanPictureBackendApplication.class, args);
    }

}
