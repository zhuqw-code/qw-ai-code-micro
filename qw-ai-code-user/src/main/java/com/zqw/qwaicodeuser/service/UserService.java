package com.zqw.qwaicodeuser.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zqw.qwaicodemother.model.dto.user.UserQueryRequest;
import com.zqw.qwaicodemother.model.entity.User;
import com.zqw.qwaicodemother.model.vo.LoginUserVO;
import com.zqw.qwaicodemother.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 服务层。
 *
 * @author <a href="#">程序员zqw</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册。
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录。
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      请求
     * @return 登录用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户登出。
     *
     * @param request 请求
     * @return 是否登出成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取登录用户信息。
     *
     * @param request 请求
     * @return 登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取加密后的密码。
     *
     * @param password 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String password);


    /**
     * 获取登录用户信息。
     *
     * @param user 用户
     * @return 登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 对单个用户信息进行脱敏
     *
     * @param user 传入的单个用户信息
     * @return 返回脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取登录用户信息。
     *
     * @param userList 用户列表
     * @return 登录用户信息
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据用户传递的查询条件拼接 QueryWrapper
     * @param userQueryRequest 用户传递的查询条件
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
