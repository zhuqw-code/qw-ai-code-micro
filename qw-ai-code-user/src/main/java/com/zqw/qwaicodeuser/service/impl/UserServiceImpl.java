package com.zqw.qwaicodeuser.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zqw.qwaicodemother.constant.UserConstant;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.exception.ThrowUtils;
import com.zqw.qwaicodemother.model.dto.user.UserQueryRequest;
import com.zqw.qwaicodemother.model.entity.User;
import com.zqw.qwaicodeuser.mapper.UserMapper;
import com.zqw.qwaicodemother.model.enums.UserRoleEnum;
import com.zqw.qwaicodemother.model.vo.LoginUserVO;
import com.zqw.qwaicodemother.model.vo.UserVO;
import com.zqw.qwaicodeuser.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务层实现。
 *
 * @author <a href="#">程序员zqw</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 参数校验
        // 1.1 参数非空判断
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 1.2 账户长度判断
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 1.3 密码长度判断 + 密码一致性判断
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不合法");
        }
        // 1.4 密码一致性判断
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        // 2. 业务判断
        // 2.1 当前账户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long cnt = userMapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(cnt > 0, ErrorCode.PARAMS_ERROR, "用户账号已存在");
        // 3. 插入数据
        // 3.1 密码加密
        String encryptPassword = this.getEncryptPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名氏");   // 默认用户名
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean isSave = this.save(user);
        /**
         * todo 后续处理
         * 有个问题就是，我是用mybatisflux进行数据库操作，并设置了逻辑删除字段，
         * 当我删除某个数据后其还会存在于数据库中，当我重新添加这条记录时就会报错。
         * 但是我在插入之前已经判断是否存在该记录，可能是默认不会判断已经被逻辑删除的数据。
         */
        ThrowUtils.throwIf(!isSave, ErrorCode.SYSTEM_ERROR, "注册失败");
        return user.getId(); // 这里能够获取到回传的id
    }

    /**
     * 用户登录。
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      请求
     * @return 登录用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        // 1.1 非空判断
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 1.2 长度判断
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 2. 业务校验
        // 2.1 获取加密的密码
        String encryptPassword = this.getEncryptPassword(userPassword);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        // 3. 操作成功存储用户信息到Session中
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 退出登录
     *
     * @param request 请求
     * @return 是否成功退出
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1. 获取用户
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        // 2. 如果有就返回最新用户，防止用户信息变更
        User user = userMapper.selectOneById(currentUser.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        return currentUser;
    }

    @Override
    public String getEncryptPassword(String password) {
        final String salt = "zqw";
        return DigestUtils.md5DigestAsHex((salt + password + salt).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (userList == null || userList.size() == 0) {
            return null;
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 请求参数为null，默认查找所有用户
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 拼接查询参数
        return QueryWrapper.create()
                    .eq("id", id)
                    .eq("userRole", userRole)
                    .like("userName", userName)
                    .like("userAccount", userAccount)
                    .like("userProfile", userProfile)
                    .orderBy(sortField, sortOrder.equals("asc"));
    }

    @Override
    public boolean updateById(User entity) {
        ThrowUtils.throwIf(entity == null, ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        boolean isUpdate = super.updateById(entity);
        // 更新后，需要将最新数据设置到session中
        if (isUpdate) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, entity);
        }
        return isUpdate;
    }
}
