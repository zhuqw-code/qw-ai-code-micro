package com.zqw.qwaicodemother.core.saver;

import com.zqw.qwaicodemother.ai.model.HtmlCodeResult;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * HTML文件保存模板，需要编写写入到本地的逻辑【HtmlCodeResult/MultiFileCodeResult】
 */
public class HtmlCodeSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>{

    @Override
    protected void validate(HtmlCodeResult result) {
        if (result.getHtmlCode() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "htmlCode is null");
        }
    }

    @Override
    protected void saveFiles(String baseDirPath, HtmlCodeResult result) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }
}
