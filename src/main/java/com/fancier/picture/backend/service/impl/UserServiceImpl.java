package com.fancier.picture.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.auth.constant.StpKit;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.mapper.UserMapper;
import com.fancier.picture.backend.model.user.User;
import com.fancier.picture.backend.model.user.dto.*;
import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author Fanfan
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-03-17 13:05:35
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private final UserMapper userMapper;

    @Value("${user.login.salt}")
    private String salt;

    @Override
    public Long registerUser(RegisterUserRequest request) {
        String userAccount = request.getUserAccount();
        // 校验账号是否存在
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount));

        ThrowUtils.throwIf(one != null, ErrorCode.PARAM_ERROR, "用户账号已存在");

        // 属性拷贝
        User user = new User();
        user.setUserAccount(request.getUserAccount());

        // 进行加密
        user.setUserPassword(encode(request.getUserPassword()));

        // 插入数据, 并返回用户 id
        save(user);
        return user.getId();
    }

    @Override
    public LoginUserVO login(UserLoginRequest request) {
        String userAccount = request.getUserAccount();
        // 校验账号是否存在
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount));
        ThrowUtils.throwIf(one == null, ErrorCode.PARAM_ERROR, "用户账号不存在");

        // 校验密码
        String userPassword = request.getUserPassword();
        String encode = encode(userPassword);
        ThrowUtils.throwIf(!one.getUserPassword().equals(encode), ErrorCode.PARAM_ERROR, "密码错误");

        // 登录
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(one, loginUserVO);

        StpKit.USER.login(loginUserVO);

        return loginUserVO;
    }

    @Override
    public Long addUser(AddUserRequest request) {
        String userAccount = request.getUserAccount();
        // 校验账号是否存在
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount));
        ThrowUtils.throwIf(one != null, ErrorCode.PARAM_ERROR, "用户账号已存在");

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setUserPassword(encode(request.getUserPassword()));
        save(user);
        return user.getId();
    }

    @Override
    public Boolean updateUser(UpdateUserRequest request) {

        Long id = request.getId();
        User oldUser = getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.PARAM_ERROR, "用户不存在");

        User user = new User();
        BeanUtils.copyProperties(request, user);

        return updateById(user);
    }

    @Override
    public Page<UserVO> getUsers(UserPageQuery pageQuery) {

        Page<User> page = userMapper.getUsers(pageQuery);

        // 转换为 UserVO
        List<UserVO> userVOS = page.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());

        // 设置分页信息
        Page<UserVO> userVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        userVOPage.setRecords(userVOS);

        return userVOPage;
    }

    @Override
    public LoginUserVO getLoginUser() {
        return (LoginUserVO) StpKit.USER.getSession().getLoginId();
    }


    @Override
    public String encode(String password) {
        return DigestUtils
                .md5DigestAsHex((salt + password).getBytes());
    }
}




