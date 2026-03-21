package com.zqw.qwaicodemother.core.parser;

import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 通过一个执行器，以及传入的枚举类型灵活控制Parser转换类型
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();
    private static final MultiFileParser MULTI_FILE_CODE_PARSER = new MultiFileParser();

    /**
     * 根据传入的枚举类型，执行不同的解析器
     * @param codeGenTypeEnum 传入的类型
     * @param codeContent 传入的代码内容
     * @return 返回解析后的结果
     */
    public static Object executeParser( String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case HTML -> HTML_CODE_PARSER.parseCode(codeContent);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的转换类型");
        };
    }
}
