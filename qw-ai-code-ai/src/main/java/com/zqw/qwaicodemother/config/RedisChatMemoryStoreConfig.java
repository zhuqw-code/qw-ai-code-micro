package com.zqw.qwaicodemother.config;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@Slf4j
// todo 测试是否能够使用ConfigurationProperties
// @ConfigurationProperties(prefix = "spring.data.redis")
public class RedisChatMemoryStoreConfig {
    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.ttl}")
    private long ttl;

    @Value("${spring.data.redis.database}")
    private int database;


    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        // log.info("Redis Config -> Host: " + host + ", Port: " + port + ", Password: " + (password != null ? password : "NULL"));
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .password(password)
                .ttl(ttl);
        // 注意：因为我们避免云服务器redis裸奔，就设置了密码，但是有密码就一定要有账号，这里设置redis默认的账号
        if (StrUtil.isNotBlank(password)) {
            builder.user("default");
        }
        return builder.build();
    }

}