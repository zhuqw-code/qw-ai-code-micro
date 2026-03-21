package com.zqw.qwaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zqw.qwaicodemother.model.dto.app.AppAddRequest;
import com.zqw.qwaicodemother.model.dto.app.AppQueryRequest;
import com.zqw.qwaicodemother.model.entity.App;
import com.zqw.qwaicodemother.model.entity.User;
import com.zqw.qwaicodemother.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="#">程序员zqw</a>
 */
public interface AppService extends IService<App> {

    /**
     * 根据实体类获取VO
     * @param app 未脱敏对象
     * @return 脱敏对象
     */
    AppVO getAppVO(App app);

    /**
     * 根据实体类列表获取VO列表
     * @param appList 未脱敏对象列表
     * @return 脱敏对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据查询条件获取QueryWrapper
     * @param appQueryRequest 带拼接的查询参数
     * @return 返回拼接好的查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 聊天生成代码
     * @param appId 应用id
     * @param userMessage 用户输入
     * @param loginUser 登录用户
     * @return 生成代码
     */
    Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser);


    /**
     * 部署应用
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 部署后的网站路径
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 异步生成应用截图
     * @param appId 应用id
     * @param appUrl 应用url
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 用户提交提示词后就会预先创建一个app应用
     * @param appAddRequest 应用创建请求
     * @param request http请求
     * @return 应用id
     */
    Long createApp(AppAddRequest appAddRequest, HttpServletRequest request);
}
