package com.zqw.qwaicodemother;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class QwAiCodeScreenshotApplication {
    public static void main(String[] args) {
        SpringApplication.run(QwAiCodeScreenshotApplication.class, args);
    }
}
