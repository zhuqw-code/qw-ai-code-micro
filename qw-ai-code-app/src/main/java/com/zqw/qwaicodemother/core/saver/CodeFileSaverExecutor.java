package com.zqw.qwaicodemother.core.saver;

import com.zqw.qwaicodemother.ai.model.HtmlCodeResult;
import com.zqw.qwaicodemother.ai.model.MultiFileCodeResult;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码保存执行器，通过传入不同的参数调用不同的保存方法
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeSaverTemplate HTML_CODE_FILE_SAVER = new HtmlCodeSaverTemplate();
    private static final MultiFileCodeSaverTemplate MULTI_FILE_CODE_SAVER = new MultiFileCodeSaverTemplate();

    public static File executeSaver(Object result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML ->
                HTML_CODE_FILE_SAVER.saveCode((HtmlCodeResult) result, appId);

            case MULTI_FILE ->
                MULTI_FILE_CODE_SAVER.saveCode((MultiFileCodeResult)result, appId);

            default ->
                throw new RuntimeException("未知的代码生成类型");

        };
    }
}
