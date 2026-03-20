package com.zqw.qwaicodeuser.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.zqw.qwaicodemother.annotation.AuthCheck;
import com.zqw.qwaicodemother.common.BaseResponse;
import com.zqw.qwaicodemother.common.DeleteRequest;
import com.zqw.qwaicodemother.common.ResultUtils;
import com.zqw.qwaicodemother.constant.UserConstant;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.exception.ThrowUtils;
import com.zqw.qwaicodemother.model.dto.user.*;
import com.zqw.qwaicodemother.model.vo.LoginUserVO;
import com.zqw.qwaicodemother.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.zqw.qwaicodemother.model.entity.User;
import com.zqw.qwaicodeuser.service.UserService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 控制层。
 *
 * @author <a href="#">程序员zqw</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    // // region 自动生成的接口
    //
    // /**
    //  * 保存。
    //  *
    //  * @param user 传入的单个用户信息
    //  * @return {@code true} 保存成功，{@code false} 保存失败
    //  */
    // @PostMapping("save")
    // public boolean save(@RequestBody User user) {
    //     return userService.save(user);
    // }
    //
    // /**
    //  * 根据主键删除。
    //  *
    //  * @param id 主键
    //  * @return {@code true} 删除成功，{@code false} 删除失败
    //  */
    // @DeleteMapping("remove/{id}")
    // public boolean remove(@PathVariable Long id) {
    //     return userService.removeById(id);
    // }
    //
    // /**
    //  * 根据主键更新。
    //  *
    //  * @param user 传入的单个用户信息
    //  * @return {@code true} 更新成功，{@code false} 更新失败
    //  */
    // @PutMapping("update")
    // public boolean update(@RequestBody User user) {
    //     return userService.updateById(user);
    // }
    //
    // /**
    //  * 查询所有。
    //  *
    //  * @return 所有数据
    //  */
    // @GetMapping("list")
    // public List<User> list() {
    //     return userService.list();
    // }
    //
    // /**
    //  * 根据主键获取。
    //  *
    //  * @param id 主键
    //  * @return 详情
    //  */
    // @GetMapping("getInfo/{id}")
    // public User getInfo(@PathVariable Long id) {
    //     return userService.getById(id);
    // }
    //
    // /**
    //  * 分页查询。
    //  *
    //  * @param page 分页对象
    //  * @return 分页对象
    //  */
    // @GetMapping("page")
    // public Page<User> page(Page<User> page) {
    //     return userService.page(page);
    // }
    // // endregion


    // region 管理员接口
    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    // endregion


    // region 用户基本操作
    /**
     * 注册。
     *
     * @param userRegisterRequest 注册参数
     * @return 返回注册成功的数据库id
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long id = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(id);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录信息
     * @param request          用于存储到Session中
     * @return 返回脱敏后的用户信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 用于获取Session中的用户信息
     * @return 返回脱敏后的用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(currentUser));
    }

    /**
     * 用户登出
     *
     * @param request 用于清除Session中的用户信息
     * @return 返回是否登出成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean isLogout = userService.userLogout(request);
        return ResultUtils.success(isLogout);
    }
    // endregion
}
