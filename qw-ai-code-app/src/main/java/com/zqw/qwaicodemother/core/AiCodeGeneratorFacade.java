package com.zqw.qwaicodemother.core;


import cn.hutool.json.JSONUtil;
import com.zqw.qwaicodemother.ai.AiCodeGeneratorService;
import com.zqw.qwaicodemother.ai.AiCodeGeneratorServiceFactory;
import com.zqw.qwaicodemother.ai.model.HtmlCodeResult;
import com.zqw.qwaicodemother.ai.model.MultiFileCodeResult;
import com.zqw.qwaicodemother.ai.model.message.AiResponseMessage;
import com.zqw.qwaicodemother.ai.model.message.ToolExecutedMessage;
import com.zqw.qwaicodemother.ai.model.message.ToolRequestMessage;
import com.zqw.qwaicodemother.core.parser.CodeParserExecutor;
import com.zqw.qwaicodemother.core.saver.CodeFileSaverExecutor;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 门面模式，用户只需要传递输出类型的枚举，就能执行对应门面方法
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    // @Resource
    // private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;


    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型  todo 关键就是这个参数，我们才能清楚调用什么方法
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 根据appId从Caffeine中获取对应的AiCodeGeneratorService
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                // generateAndSaveHtmlCode(userMessage);
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                // yield CodeFileSaver.saveHtmlCodeResult(result);
                yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                // generateAndSaveMultiFileCode(userMessage);
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                // yield CodeFileSaver.saveMultiFileCodeResult(result);
                yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }




    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 根据appId从Caffeine中获取对应的AiCodeGeneratorService              // todo 这里不传默认使用HTML生成类型，观察是否报错
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                // generateAndSaveHtmlCodeStream(userMessage);
                Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(htmlCodeStream, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                // generateAndSaveMultiFileCodeStream(userMessage);
                Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(multiFileCodeStream, codeGenTypeEnum, appId);
            }
            case VUE_PROJECT -> {
                // generateAndSaveVueProjectCodeStream(userMessage);
                TokenStream vueProjectCodeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(vueProjectCodeStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {   // 适配器
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        String jsonStr = JSONUtil.toJsonStr(aiResponseMessage);
                        sink.next(jsonStr);
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }



    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(chunk -> {
                    // 实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    try {
                        String completeCode = codeBuilder.toString();
                        // 解析代码
                        // HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                        // todo 这里我们不知道返回结果是 HtmlCodeResult / MultiFileCodeResult，所以使用Object接收
                        //  并且这里不能使用泛型设置具体返回类型，因为我们不知道这里传入的是什么类型数据【
                        //   -----具体的策略/模板方法子类我们知道具体的某个策略或模板方法需要使用泛型定义参数/返回值，
                        //   但是在执行器中我们并不知道当前执行的是什么策略/模板方法子类 所以不能使用泛型定义(因为我们不知道)----】
                        Object parserResult = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                        // 保存代码到文件
                        // File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                        File file = CodeFileSaverExecutor.executeSaver(parserResult, codeGenTypeEnum, appId);
                        log.info("代码文件保存成功: {}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败: {}", e.getMessage());
                    }
                });
    }


    // region 之前的流式门面方法
    // /**
    //  * 统一入口：根据类型生成并保存代码（流式）
    //  *
    //  * @param userMessage     用户提示词
    //  * @param codeGenTypeEnum 生成类型
    //  */
    // public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
    //     if (codeGenTypeEnum == null) {
    //         throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
    //     }
    //     return switch (codeGenTypeEnum) {
    //         case HTML -> generateAndSaveHtmlCodeStream(userMessage);
    //         case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
    //         default -> {
    //             String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
    //             throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
    //         }
    //     };
    // }
    // endregion


    // region 普通输出，非流式 html/multiFile
    // /**
    //  * 生成 HTML 模式的代码并保存
    //  *
    //  * @param userMessage 用户提示词
    //  * @return 保存的目录
    //  */
    // private File generateAndSaveHtmlCode(String userMessage) {
    //     HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
    //     return CodeFileSaver.saveHtmlCodeResult(result);
    // }
    //
    // /**
    //  * 生成多文件模式的代码并保存
    //  *
    //  * @param userMessage 用户提示词
    //  * @return 保存的目录
    //  */
    // private File generateAndSaveMultiFileCode(String userMessage) {
    //     MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
    //     return CodeFileSaver.saveMultiFileCodeResult(result);
    // }
    // endregion


    // region 流式生成html/multiFile代码
    // /**
    //  * 生成 HTML 模式的代码并保存（流式）
    //  *
    //  * @param userMessage 用户提示词
    //  * @return 保存的目录
    //  */
    // private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
    //     Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
    //     // 当流式返回生成代码完成后，再保存代码
    //     StringBuilder codeBuilder = new StringBuilder();
    //     return result
    //             .doOnNext(chunk -> {
    //                 // 实时收集代码片段
    //                 codeBuilder.append(chunk);
    //             })
    //             .doOnComplete(() -> {
    //                 // 流式返回完成后保存代码
    //                 try {
    //                     String completeHtmlCode = codeBuilder.toString();
    //                     HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
    //                     // 保存代码到文件
    //                     File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    //                     log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
    //                 } catch (Exception e) {
    //                     log.error("保存失败: {}", e.getMessage());
    //                 }
    //             });
    // }
    //
    // /**
    //  * 生成多文件模式的代码并保存（流式）
    //  *
    //  * @param userMessage 用户提示词
    //  * @return 保存的目录
    //  */
    // private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
    //     Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
    //     // 当流式返回生成代码完成后，再保存代码
    //     StringBuilder codeBuilder = new StringBuilder();
    //     return result
    //             .doOnNext(chunk -> {
    //                 // 实时收集代码片段
    //                 codeBuilder.append(chunk);
    //             })
    //             .doOnComplete(() -> {
    //                 // 流式返回完成后保存代码
    //                 try {
    //                     String completeMultiFileCode = codeBuilder.toString();
    //                     MultiFileCodeResult multiFileResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
    //                     // 保存代码到文件
    //                     File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileResult);
    //                     log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
    //                 } catch (Exception e) {
    //                     log.error("保存失败: {}", e.getMessage());
    //                 }
    //             });
    // }
    // endregion

}

