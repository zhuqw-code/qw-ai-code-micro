package com.zqw.qwaicodemother.core.parser;

/**
 * 解析ai返回结果的策略接口
 */
public interface CodeParser<T> {

    T parseCode(String codeContent);

}
