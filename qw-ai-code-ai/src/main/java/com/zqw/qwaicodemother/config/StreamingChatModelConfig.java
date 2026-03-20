package com.zqw.qwaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 编写 Vue工程化深度思考模型
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data         // 不设置get/set方法，即使能够读取到yml配置文件的信息，框架也不能给我们注入
public class StreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private Double temperature;

    private boolean logRequests;

    private boolean logResponses;


    @Bean
    @Scope("prototype") // 每次请求都会创建一个新的对象 todo 具体执行什么方法才能获取？
    public StreamingChatModel streamingChatModelPrototype() {
        // final String modelName = "deepseek-reasoner";
        // final int maxTokens = 8192;

        // final String modelName = "deepseek-chat";
        // final int maxTokens = 8192;       // 不能超过8192否则请求无法被deepseek大模型接收
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}