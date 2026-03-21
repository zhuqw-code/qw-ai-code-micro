package com.zqw.qwaicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 通用的命令行执行类
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 异步构建vue项目
     *
     * @param projectPath 待构建的vue项目路径
     * todo: 思考：什么时候调用这个工具进行项目构建  ==>  肯定是vue项目生成保存完成
     */
    public final void buildProjectAsync(String projectPath){
        try {
            Thread.ofVirtual().name("vue_project" + System.currentTimeMillis())
                    .start(() -> {
                        buildProject(projectPath);
                    });
        } catch (Exception e) {
            log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
        }
    }



    /**
     *
     * 总的调用流程
     * @param projectPath
     * @return
     */
    public final boolean buildProject(String projectPath){
        // 1. 校验文件夹是否存在
        File projectDir = new File(projectPath);
        if(!projectDir.exists() || !projectDir.isDirectory()){
            log.error("项目目录不存在: {}", projectPath);
            return false;
        }
        // 2. 判断是否生成package.json文件
        File jsonFile = new File(projectDir, "package.json");
        if (!jsonFile.exists()) {
            log.error("package.json 文件不存在: {}", jsonFile.getAbsolutePath());
            return false;
        }

        // 3. 执行 `npm install` 命令
        log.info("开始构建 Vue 项目: {}", projectPath);
        if(!executeNpmInstall(projectDir)){
            log.error("执行 npm install 失败");
            return false;
        }
        // 4. 执行 ``npm run build`` 命令
        if(!executeNpmBuild(projectDir)){
            log.error("执行 npm run build 失败");
            return false;
        }
        // 5. 判断是否成功生成dist目录
        File distDir = new File(projectPath, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成但 dist 目录未生成: {}", distDir.getAbsolutePath());
            return false;
        }
        log.info("Vue 项目构建成功，dist 目录: {}", distDir.getAbsolutePath());
        return true;
    }



    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }


    /**
     * 拼接基本命令
     * @param baseCommand 基本命令
     * @return 返回特殊系统的命令
     */
    private String buildCommand(String baseCommand) {
        return isWindows() ? baseCommand + ".cmd" : baseCommand;
    }


    /**
     * 判断是否为windows系统，因为不同系统执行npm install的命令不同. windows: npm.cmd install, linux: npm install
     * @return 是否是
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }


    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            // 1. 调用RuntimeUtil
            Process process = RuntimeUtil.exec(null, workingDir, command.split("\\s+"));
            // 2. 设置超时等待时间
            boolean isSuccess = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            // 3. 判断是否超时
            if (!isSuccess) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();     // 强制终止进程
                return false;
            }
            // 4. 是否正常退出
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (InterruptedException e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }
}