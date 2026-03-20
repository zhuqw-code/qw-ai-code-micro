package com.zqw.qwaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;


@Description("生成Html代码的结果")
@Data
public class HtmlCodeResult {

    @Description("html代码")
    private String htmlCode;

    @Description("描述")
    private String description;
}
