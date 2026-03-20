package com.zqw.qwaicodeuser.aop;

import com.zqw.qwaicodemother.annotation.AuthCheck;
import com.zqw.qwaicodemother.constant.UserConstant;
import com.zqw.qwaicodemother.exception.BusinessException;
import com.zqw.qwaicodemother.exception.ErrorCode;
import com.zqw.qwaicodemother.model.entity.User;
import com.zqw.qwaicodemother.model.enums.UserRoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    // 切面类
    // 拦截所有带有AuthCheck注解的方法
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint proceeding, AuthCheck authCheck) throws Throwable {
        // 1. 当前方法的权限
        String mustRole = authCheck.mustRole();
        // 2. 获取用户，进而获取到用户role
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) attribute;

        // 3. 权限判断
        // 3.1 不需要权限，任何人都能访问
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) {
            return proceeding.proceed();
        }
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "没有登录，请先登录");
        }
        // 3.2 需要权限，判断当前用户是否拥有权限
        String userRole = currentUser.getUserRole();
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) &&
                !UserRoleEnum.ADMIN.equals(UserRoleEnum.getEnumByValue(userRole))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限，请联系管理员");
        }
        return proceeding.proceed();
    }
}
