package com.zqw.qwaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zqw.qwaicodemother.constant.AppConstant;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;


/**
 * 模板方法模式中的抽象方法
 */
public abstract class CodeFileSaverTemplate<T>{

    /**
     * 抽象模板类的主流程方法
     * @param result 生成代码的结果
     */
    public final File saveCode(T result, Long appId) {
        // 1. 对待保存文件进行校验
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "待保存文件为空");
        }
        validate(result);
        // 2. 获取文件路径
        String filePath = buildUniqueDir(appId);// 业务类型
        // 3. 保存文件
        saveFiles(filePath, result);
        // 4. 返回文件
        return new File(filePath);
    }


    // 获取当前路径
    private static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    // 创建唯一目录 不能被子类重写
    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    protected final String buildUniqueDir(Long appId) {
        String bizType = getCodeType().getValue();
        // String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        // todo 替换雪花算法：因为我们需要将 应用生成 整合 应用管理
        String uniqueDirName = StrUtil.format("{}_{}", bizType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    // 保存文件逻辑 不能被子类重写
    /**
     * 写入单个文件
     */
    protected final void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 能被子类重写
     * @param result
     */
    protected void validate(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "result is null");
        }
    }

    /**
     * 由子类自定义写入文件
     * @param filePath 待保存的文件路径
     * @param result 待保存文件【HtmlCodeResult/MultiFileCodeResult】
     */
    protected abstract void saveFiles(String filePath, T result);

    /**
     * 由子类自定义写入文件
     *
     * @return 文件保存类型
     */
    protected abstract CodeGenTypeEnum getCodeType();
}
