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
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data         // 不设置get/set方法，即使能够读取到yml配置文件的信息，框架也不能给我们注入
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private Double temperature;

    private Boolean logRequests = false;

    private Boolean logResponses = false;


    @Bean
    @Scope("prototype")    // 将所有用到ai大模型的地方都设置为多例模式
    public StreamingChatModel reasoningStreamingChatModelPrototype() {
        // final String modelName = "deepseek-reasoner";
        // final int maxTokens = 8192;

        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;       // 不能超过8192否则请求无法被deepseek大模型接收
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