package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancier.picture.backend.model.user.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.user.dto.*;
import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import com.fancier.picture.backend.model.user.vo.UserVO;

/**
* @author Fanfan
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-03-17 13:05:35
*/
public interface UserService extends IService<User> {

    Long registerUser(RegisterUserRequest request);

    Boolean isAdmin();

    String encode(String password);


    LoginUserVO login(UserLoginRequest request);

    Long addUser(AddUserRequest request);

    Boolean updateUser(UpdateUserRequest request);

    Page<UserVO> getUsers(UserPageQuery pageQuery);

    LoginUserVO getLoginUser();
}
