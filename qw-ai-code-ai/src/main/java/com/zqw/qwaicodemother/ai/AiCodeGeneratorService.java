package com.zqw.qwaicodemother.ai;


import com.zqw.qwaicodemother.ai.model.HtmlCodeResult;
import com.zqw.qwaicodemother.ai.model.MultiFileCodeResult;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * Interface for AI code generation service that provides various methods to generate different types of code.
 * This service includes methods for generating single code, HTML code, multi-file code, and streaming versions of HTML and multi-file code generation.
 * The step of sending a prompt to the AI and receiving a response is handled by the service.
 * 1.用户输入： “帮我做个登录页”
 * 2.发送给 AI： (System Prompt + 用户输入)
 * 3.AI 返回： 字符串 "HTML"
 * 4.框架拦截： 收到字符串 "HTML"
 * 5.框架执行： CodeGenTypeEnum.valueOf("HTML")
 * 6.你得到： 对象 CodeGenTypeEnum.HTML
 */
public interface AiCodeGeneratorService {

    /**
     * Generates code based on the user's message.
     *
     * @param userMessage The input message from the user containing code generation request
     * @return Generated code as a String
     */
    String generateCode(String userMessage);

    /**
     * 生成html代码
     * @param userMessage 用户输入
     * @return 通过设置的返回值类型，能够让ai返回结构化数据
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户输入
     * @return 通过设置的返回值类型，能够让ai返回结构化数据
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成 HTML 代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成 Vue 项目代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成过程的流式响应
     * 注意：如果让ai维护appId必须添加@MemoryId注解，否则ai无法维护appId，并且用户信息也要使用@UserMessage注解
     * todo 这里非常重要，如果不使用@MemoryId进行表示，就会导致在调用FileWriteTool时不知道需要创建路径的appId参数!!!!!!!!
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);
}
