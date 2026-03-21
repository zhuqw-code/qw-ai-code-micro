package com.zqw.qwaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zqw.qwaicodemother.ai.AiCodeGenTypeRoutingService;
import com.zqw.qwaicodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.zqw.qwaicodemother.constant.AppConstant;
import com.zqw.qwaicodemother.core.AiCodeGeneratorFacade;
import com.zqw.qwaicodemother.core.builder.VueProjectBuilder;
import com.zqw.qwaicodemother.core.handler.StreamHandlerExecutor;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.exception.ThrowUtils;
import com.zqw.qwaicodemother.innerservice.InnerScreenshotService;
import com.zqw.qwaicodemother.innerservice.InnerUserService;
import com.zqw.qwaicodemother.mapper.AppMapper;
import com.zqw.qwaicodemother.model.dto.app.AppAddRequest;
import com.zqw.qwaicodemother.model.dto.app.AppQueryRequest;
import com.zqw.qwaicodemother.model.entity.App;
import com.zqw.qwaicodemother.model.entity.User;
import com.zqw.qwaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.zqw.qwaicodemother.model.enums.CodeGenTypeEnum;
import com.zqw.qwaicodemother.model.vo.AppVO;
import com.zqw.qwaicodemother.model.vo.UserVO;
import com.zqw.qwaicodemother.service.AppService;
import com.zqw.qwaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 *
 * @author <a href="#">程序员zqw</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
        implements AppService{

    // @Resource
    // private UserService userService;
    // @Resource
    // @Lazy
    @DubboReference
    private InnerUserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    // 对Flux流进行不同解析
    @Resource
    public StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    // 截图服务
    // @Resource
    // private ScreenshotService screenshotService;
    // @Resource
    // @Lazy
    @DubboReference
    private InnerScreenshotService screenshotService;

    @Value("${code.deploy-host:http://localhost}")
    private String deployHost;



    // @Resource
    // private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;
    // 使用工厂中的方法保证每个线程创建一个路由服务
    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 1. 获取到所有用户id
        Set<Long> ids = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        // 2. 根据id查询用户信息并转化为key=userId, value = UserVO对象
        Map<Long, UserVO> userVOMap = userService.listByIds(ids)
                .stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        // 3. 封装appVO中的userVO字段
        return appList.stream()
                .map(app -> {
                    AppVO appVO = getAppVO(app);
                    appVO.setUser(userVOMap.get(app.getUserId()));
                    return appVO;
                })
                .collect(Collectors.toList());

        // // 批量获取用户信息，避免 N+1 查询问题
        // Set<Long> userIds = appList.stream()
        //         .map(App::getUserId)
        //         .collect(Collectors.toSet());
        // Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
        //         .collect(Collectors.toMap(User::getId, userService::getUserVO));
        // return appList.stream().map(app -> {
        //     AppVO appVO = getAppVO(app);
        //     UserVO userVO = userVOMap.get(app.getUserId());
        //     appVO.setUser(userVO);
        //     return appVO;
        // }).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }




    /**
     * 调用ai生成，之后调用门面类的方法将生成信息保存到数据库中
     * @param appId 应用id
     * @param userMessage 用户输入
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser){
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 参数校验
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 4~5 将用户消息添加到数据库中
        chatHistoryService.addChatMessage(appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 5. 调用ai生成代码，调用门面类生成应用，并保存生成的代码文件
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);
        // 6. 在ai生成信息后将信息关联添加到数据库中
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        // return contentFlux
        //         .doOnNext(chunk -> {
        //             // 实时收集代码片段
        //             codeBuilder.append(chunk);
        //         })
        //         .doOnComplete(() -> {
        //             // 流式返回完成后保存代码
        //             try {
        //                 String aiContent = codeBuilder.toString();
        //                 if (StringUtils.isBlank(aiContent)) {
        //                     throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI生成代码内容为空");
        //                 }
        //                 // 添加ai响应信息到数据库
        //                 boolean isSave = chatHistoryService.addChatMessage(appId, aiContent, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        //                 log.info("ai聊天记录保存数据库成功：{}", isSave);
        //             } catch (Exception e) {
        //                 log.error("保存失败: {}", e.getMessage());
        //             }
        //         })
        //         .doOnError(error -> {
        //             String errorMessage = "用户恢复消息失败";
        //             chatHistoryService.addChatMessage(appId, errorMessage, CodeGenTypeEnum.MULTI_FILE.getValue(), loginUser.getId());
        //         });
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }

    /**
     * 根据之前生成好的应用的id，部署应用
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 部署后的网站路径
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1.校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "登录用户不能为空");
        // 2.判断应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        // 3.用户是否有权限部署
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户没有权限部署该应用");
        }
        // 4.生成deployKey
        String deployKey = app.getDeployKey();
        if (StringUtils.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5.获取源文件路径
        String codeType = app.getCodeGenType();
        String sourceDirName = StrUtil.format("{}_{}", codeType, appId);
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6.检查源文件是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        // todo 6~7.这个是在真正点击部署按钮后才执行【真正部署】
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeType);
        if (codeGenTypeEnum!= null && codeGenTypeEnum.equals(CodeGenTypeEnum.VUE_PROJECT)){
            // 需要重新构建
            boolean isSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!isSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 判断是否存在dist
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            sourceDir = distDir;   // 设置最新的dist目录
        }

        // 7.将源文件部署到部署文件夹下
        // deployKey 在不同环境下需要设置不同的地址，因为我们在nginx设置只有洲121.41.14.209才会被nginx反向代理，所以必须要设置真实的服务器ip地址
        // String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;

        // 10. 构建应用访问 URL
        // String deployDirPath = String.format("%s/%s/", , deployKey);
        FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        // 8.将deployKey等信息保存到数据库
        app.setDeployKey(deployKey);
        app.setDeployedTime(LocalDateTime.now());
        boolean isUpdate = this.updateById(app);
        ThrowUtils.throwIf(!isUpdate, ErrorCode.OPERATION_ERROR, "部署后更新失败");
        // 9.返回部署后的网站路径
        // String deployUrl = AppConstant.CODE_DEPLOY_HOST + "/" + deployKey;
        String deployUrl = deployHost + "/" + deployKey;
        // 10.部署成功后，调用截图服务进行异步截图
        generateAppScreenshotAsync(appId, deployUrl); // 将deployUrl给到工具类进行截图
        return deployUrl;
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     * todo 注意：如果我们将这个异步方法放到ScreenshotService中，需要在业务逻辑中等待这个异步结果，后续还要操作数据库
     *      干脆我们直接在appServiceImpl中直接将等待异步条件结果 + 更新数据库封装在一起。
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    /**
     * todo 开始逻辑写在controller层，但是由于需要调用不同的service就被抽取到service层了
     * @param appAddRequest 应用创建请求
     * @param request http请求
     * @return 应用id
     */
    @Override
    public Long createApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 获取当前登录用户
        User loginUser = InnerUserService.getLoginUser(request);
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用ai + 框架获取应用的生成类型 暂时设置为多文件生成，后续进行选择
        // todo 保证每个线程一个路由服务
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        // CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        // app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());
        app.setCodeGenType(codeGenTypeEnum.getValue());   // 设置到数据库中
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), codeGenTypeEnum.getValue());
        return app.getId();
    }

    /**
     * 通过重写自带的删除逻辑，增加删除app时自动删除聊天记录
     * @param id 应用ID
     * @return 删除结果
     */
    @Override
    public boolean removeById(Serializable id) {
        // 1. 参数校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "待删除应用ID不能为空");
        // 2. 转换参数
        Long appId = Long.parseLong(id.toString());
        // 3. 删除对话记录
        try {
            chatHistoryService.deleteAppById(appId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "关联删除聊天记录失败");
        }
        // 4. 删除应用
        return super.removeById(appId);
    }
}
