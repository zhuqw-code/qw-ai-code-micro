package com.zqw.qwaicodeuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.zqw.qwaicodeuser.mapper")
@ComponentScan("com.zqw")
public class QWAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(QWAiCodeUserApplication.class, args);
    }
}
