package com.zqw.qwaicodeuser;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.zqw.qwaicodeuser.mapper")
@ComponentScan("com.zqw")
@EnableDubbo
// 1.编写内部业务调用接口并实现  2.DubboService注解  3. 启动类上添加EnableDubbo
public class QWAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(QWAiCodeUserApplication.class, args);
    }
}
