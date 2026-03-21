package com.zqw.qwaicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zqw.qwaicodemother.ai.guardrail.PromptSafetyInputGuardrail;
import com.zqw.qwaicodemother.ai.tools.ToolManager;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;
import com.zqw.qwaicodemother.service.ChatHistoryService;
import com.zqw.qwaicodemother.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory{

    // @Resource  // 因为路由服务中使用的大模型不需要太智能，所以使用普通chat-model就行，导致这里会有多个ChatModel注入，解决方法就是指定名称注入
    @Resource(name = "openAiChatModel")
    private ChatModel chatmodel;

    // 由于会加载openAiStreamingChatModel，我们又添加了深度思考模型，为了防止依赖注入冲突，这里使用具体的Bean名称
    // 这里要注入的大模型都需要使用多例的，提高系统并发性
    // @Resource
    // private StreamingChatModel openAiStreamingChatModel;
    //
    // @Resource
    // private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * 为每一个应用创建一个AiCodeGeneratorService实例，存储到Caffeine容器中
     */
    // 因为涉及到代码生成类型，这里把CodeGenTypeEnum作为参数传入用来构建Caffeine的key
    private final Cache<String, AiCodeGeneratorService> aiServiceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务示例被移除，cacheKey: {}, cause: {}", key, cause);
            })
            .build();

    /**
     * map 的get方法
     * @param appId key
     * @return value
     */
    public AiCodeGeneratorService getAiCodeGeneratorService (Long appId){
        // 返回之前加载之前的聊天记录
        // return aiServiceCache.get(appId, this::createAiCodeGeneratorService); // 有就拿，没有就创建
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * map 的get方法
     * @param appId key
     * @return value
     */
    public AiCodeGeneratorService getAiCodeGeneratorService (Long appId, CodeGenTypeEnum codeGenTypeEnum){
        String cacheKey = buildCacheKey(appId, codeGenTypeEnum);
        // 返回之前加载之前的聊天记录
        return aiServiceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenTypeEnum)); // 有就拿，没有就创建
    }


    /**
     * 为每个对话生成一个AiCodeGeneratorService
     * @param appId 用户id
     * @return 返回应用对话
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenTypeEnum){
        log.info("创建新的AI服务示例，appId: {}", appId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)     // 关联redis缓存
                .maxMessages(20)
                .build();

        // todo 添加该应用的历史对话记录
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 10);

        return switch(codeGenTypeEnum) {
            case VUE_PROJECT -> {
                StreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .streamingChatModel(reasoningStreamingChatModel)   // 关联流式模型
                        .chatMemoryProvider(memoryId -> chatMemory)        // 每个memoryId对应一个chatMemory
                        .tools(
                                // new FileWriteTool(),
                                // new FileReadTool(),
                                // new FileModifyTool(),
                                // new FileDirReadTool(),
                                // new FileDeleteTool()
                                toolManager.getAllTools()
                        )
                        .maxSequentialToolsInvocations(20)   // 设置工具最多调用次数
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> {
                            return ToolExecutionResultMessage.from(toolExecutionRequest,
                                    "Error: there is no tool called:" + toolExecutionRequest.name());
                        })
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        .build();
            }
            case HTML, MULTI_FILE -> {
                StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatmodel)
                        .streamingChatModel(openAiStreamingChatModel)   // 关联流式模型
                        .chatMemory(chatMemory)
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        .build();
            }
            default -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型");
            }
        };
    }

    /**
     * 创建AiCodeGeneratorService实例
     * @return 返回一个AiCodeGeneratorService实例
     */
    // @Bean
    // public AiCodeGeneratorService aiCodeGeneratorService(){
    //     // 1. 底层通过反射，获取到AiCodeGeneratorService.class，并通过 chatModel 获取到具体实现类，注入代理对象
    //     // return AiServices.create(AiCodeGeneratorService.class, chatmodel);
    //     return AiServices.builder(AiCodeGeneratorService.class)
    //             .chatModel(chatmodel)
    //             .streamingChatModel(openAiStreamingChatModel)
    //             .build();
    // }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}

