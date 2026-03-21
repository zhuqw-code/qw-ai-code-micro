package com.zqw.qwaicodemother.ai;

import com.zqw.qwaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由服务工厂
 *
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    // @Resource      // 这里使用创建的路由模型（本质还是简单对话模型）
    @Resource(name = "routingChatModelPrototype")
    private ChatModel chatModel;

    /**
     * 创建AI代码生成类型路由服务实例
     */
    // @Bean   // 思考这里需要让Spring管理吗？如果让其管理代表我们全局就一个AiCodeGenTypeRoutingService，还是导致并发问题
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        ChatModel routingChatModelPrototype = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(routingChatModelPrototype)
                .build();
    }

    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService(){
        return createAiCodeGenTypeRoutingService();
    }
}
