package com.zqw.qwaicodemother.service.impl;

import com.zqw.qwaicodemother.innerservice.InnerScreenshotService;
import com.zqw.qwaicodemother.service.ScreenshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@DubboService
@Slf4j
public class InnerScreenshotServiceImpl implements InnerScreenshotService {

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {

        return screenshotService.generateAndUploadScreenshot(webUrl);
    }
}

