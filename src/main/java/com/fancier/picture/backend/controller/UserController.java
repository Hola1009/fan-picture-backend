package com.fancier.picture.backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancier.picture.backend.common.BaseResponse;
import com.fancier.picture.backend.common.DeleteRequest;
import com.fancier.picture.backend.common.ResultUtils;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.constant.UserRole;
import com.fancier.picture.backend.model.user.User;
import com.fancier.picture.backend.model.user.dto.*;
import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;



    @PostMapping("/register")
    public BaseResponse<Long> registerUser(@Validated @RequestBody RegisterUserRequest request) {
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                new IllegalArgumentException("两次密码不一致"));

        return ResultUtils.success(userService.registerUser(request));
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@Validated @RequestBody UserLoginRequest request) {
        return ResultUtils.success(userService.login(request));
    }

    @GetMapping("/get/login")
    public BaseResponse<?> getLoginUser() {StpUtil.getLoginId();
        Object loginId = StpUtil.getSession().getLoginId();
        return ResultUtils.success(loginId);
    }
    @GetMapping("/logout")
    public BaseResponse<?> logout() {
        StpUtil.logout();
        return ResultUtils.success(true);
    }


    @PostMapping("/add")
    @SaCheckRole(UserRole.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@Validated @RequestBody AddUserRequest request) {
        return ResultUtils.success(userService.addUser(request));
    }
    @GetMapping("/get")
    @SaCheckRole(UserRole.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        User byId = userService.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAM_ERROR, "用户不存在");
        return ResultUtils.success(byId);
    }

    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        User byId = userService.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAM_ERROR, "用户不存在");
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(byId, userVO);

        return ResultUtils.success(userVO);
    }

    @PostMapping("/delete")
    @SaCheckRole(UserRole.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@Validated @RequestBody DeleteRequest request) {
        return ResultUtils.success(userService.removeById(request.getId()));
    }

    @PostMapping("/update")
    @SaCheckRole(UserRole.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@Validated @RequestBody UpdateUserRequest request) {
        return ResultUtils.success(userService.updateUser(request));
    }

    @PostMapping("/list/page/vo")
    @SaCheckRole(UserRole.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> getUsers(@Validated @RequestBody UserPageQuery pageQuery) {
        return ResultUtils.success(userService.getUsers(pageQuery));
    }

}
