package com.zqw.qwaicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * 设置无头浏览器，对目标网站进行仿用户操作
 *  优化：如果高并发情况下，很多请求打过来，我们只用一个WebDriver肯定是不行的
 *  1. 每个请求创建一个webDriver
 *  2. 维护WebDriver连接池
 *  3. 使用ThreadLocal
 */
@Slf4j
public class WebScreenshotUtils {

    // private static final WebDriver webDriver;

    private static final ThreadLocal<WebDriver> webDriverMap = new ThreadLocal<>(){
        // 重写get逻辑
        @Override
        public WebDriver get() {
            WebDriver webDriver = super.get();
            if (webDriver == null){
                webDriver = initChromeDriver();
                webDriverMap.set(webDriver);
            }
            return webDriver;
        }
    };

    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;

    /**
     * 类加载就获取该线程的WebDriver操作对象
     */
    private static final WebDriver webDriver = webDriverMap.get();

    //
    // static {
    //     final int DEFAULT_WIDTH = 1600;
    //     final int DEFAULT_HEIGHT = 900;
    //     webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    // }

    // 项目销毁前关闭
    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    public static String saveWebPageScreenshot(String webUrl){
        try {
            // 参数校验
            if (webUrl == null || webUrl.isEmpty()) {
                log.error("webUrl is null or empty");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "webUrl不能为空");
            }
            // 创建临时文件目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshot" + File.separator
                    + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            // 创建文件名
            final String IMAGE_SUFFER = ".png";
            String savePath = rootPath + File.separator + RandomUtil.randomNumbers(8) + IMAGE_SUFFER;
            // 访问网页
            webDriver.get(webUrl);
            // 等待页面加载成功
            waitForPageLoad(webUrl);
            // 获取网页截图
            byte[] imageBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            // 保存截图
            saveImage(imageBytes, savePath);
            log.info("保存原始截图成功，路径为：{}", savePath);
            // 压缩图片，保存
            final String COMPRESS_SUFFER = "_compressed.jpg";
            String compressPath = rootPath + File.separator + RandomUtil.randomNumbers(8) + COMPRESS_SUFFER;
            compressImage(savePath, compressPath);
            // 销毁原始文件
            FileUtil.del(savePath);
            // 返回压缩后的文件路径
            return compressPath;
        } catch (Exception e){
            log.error("截图失败，原因：{}", e.getMessage());
            return null;
        }
    }


    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver() {
        try {
            // 自动管理 ChromeDriver
            // WebDriverManager.chromedriver().setup();
            WebDriverManager.chromedriver()
                    .setup();
            // WebDriverManager.edgedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // EdgeOptions options = new EdgeOptions();
            // 无头模式
            options.addArguments("--headless");      // 设置无头浏览器，减低内存消耗
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", DEFAULT_WIDTH , DEFAULT_HEIGHT));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // options.addArguments("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // EdgeDriver driver = new EdgeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 等待无头浏览器加载页面。只有加载完成才能下载网页土拍你
     * @param webUrl 目标网站
     */
    public static void waitForPageLoad(String webUrl){
        try {
            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            webDriverWait.until(
                    driver -> ((JavascriptExecutor) driver)
                            .executeScript("return document.readyState")
                            .equals("complete")
            );
            // 额外等待几秒，防止一些页面没有正常加载
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("等待页面加载失败，继续等待截图", e);
        }
    }

    /**
     * 保存图片到指定目录
     * @param imageBytes 待保存的文件
     * @param imagePath 保存路径
     */
    public static void saveImage(byte[] imageBytes, String imagePath){
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (IORuntimeException e) {
            log.error("保存图片失败，待保存文件目录{}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩通过无头浏览器保存到本地的图片
     * @param originImagePath 保存到本地的，待压缩图片路径
     * @param compressImagePath 压缩后的图片路径
     */
    public static void compressImage(String originImagePath, String compressImagePath){
        final float quality = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originImagePath),
                    FileUtil.file(compressImagePath),
                    quality
            );
        } catch (IORuntimeException e) {
            log.info("压缩图片失败，待压缩图片路径{} -> {}", originImagePath, compressImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }
}
