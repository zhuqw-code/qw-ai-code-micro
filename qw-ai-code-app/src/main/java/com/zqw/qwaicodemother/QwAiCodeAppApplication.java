package com.zqw.qwaicodemother;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// 不开启会因为host为空报错
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.zqw.qwaicodemother.mapper")
@EnableCaching
public class QwAiCodeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(QwAiCodeAppApplication.class, args);
    }
}
