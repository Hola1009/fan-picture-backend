package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.user.User;
import com.fancier.picture.backend.model.user.dto.*;
import com.fancier.picture.backend.model.user.vo.UserVO;

import java.util.List;

/**
* @author Fanfan
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-03-17 13:05:35
*/
public interface UserService extends IService<User> {

    Long registerUser(RegisterUserRequest request);

    UserVO getUserVO(Long id);

    String encode(String password);


    UserVO login(UserLoginRequest request);

    Long addUser(AddUserRequest request);

    Boolean updateUser(UpdateUserRequest request);

    Page<UserVO> getUsers(UserPageQuery pageQuery);

    UserVO getLoginUser();

    List<UserVO> listVOByIds(List<Long> userIds);

    Boolean editUser(UserEditRequest request);

    Boolean sendValidationCode(SendValidationCodeRequest request);
}
