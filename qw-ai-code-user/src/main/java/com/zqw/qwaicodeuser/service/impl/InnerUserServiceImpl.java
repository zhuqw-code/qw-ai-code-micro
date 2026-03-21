package com.zqw.qwaicodeuser.service.impl;

import com.zqw.qwaicodemother.innerservice.InnerUserService;
import com.zqw.qwaicodemother.model.entity.User;
import com.zqw.qwaicodemother.model.vo.UserVO;
import com.zqw.qwaicodeuser.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVO(user);
    }
}
