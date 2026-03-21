package com.zqw.qwaicodemother.core.saver;

import com.zqw.qwaicodemother.ai.model.MultiFileCodeResult;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;
import org.apache.commons.lang3.StringUtils;

public class MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected void validate(MultiFileCodeResult result) {
        if (StringUtils.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "htmlCode or cssCode or jsCode is null，but htmlCode is force required");
        }
    }

    @Override
    protected void saveFiles(String baseDirPath, MultiFileCodeResult result) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }
}
