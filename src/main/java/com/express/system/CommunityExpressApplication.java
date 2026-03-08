package com.express.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.express.system.mapper")
public class CommunityExpressApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityExpressApplication.class, args);
    }

}
