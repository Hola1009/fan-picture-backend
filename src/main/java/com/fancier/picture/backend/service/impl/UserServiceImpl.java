package com.fancier.picture.backend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.auth.constant.StpKit;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.mapper.UserMapper;
import com.fancier.picture.backend.model.user.User;
import com.fancier.picture.backend.model.user.dto.*;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.UserService;
import com.fancier.picture.backend.thirdparty.javaMail.MailManager;
import com.fancier.picture.backend.util.FileParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

    private final StringRedisTemplate stringRedisTemplate;

    private final MailManager mailManager;

    private static final String MAIL_HTML_TEMPLATE;
    private static final String VALIDATION_CODE_KEY_PREFIX = "user:validationCode:";

    static {
        MAIL_HTML_TEMPLATE = FileParserUtil.parseStringFromResource("biz/mailTemplate.txt");
    }

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
    public UserVO login(UserLoginRequest request) {
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
        UserVO loginUserVO = new UserVO();
        BeanUtils.copyProperties(one, loginUserVO);

        StpKit.USER.login(loginUserVO.getId());
        StpKit.USER.getSession().set("loginUser", loginUserVO);
        StpKit.SPACE.login(loginUserVO.getId());

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
    public UserVO getLoginUser() {
        UserVO loginUserVO = (UserVO) StpKit.USER.getSession().get("loginUser");
        // 防止未登录造成的空指针异常
        return Optional.of(loginUserVO).orElseGet(UserVO::new);
    }

    @Override
    public List<UserVO> listVOByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> users = this.listByIds(userIds);

        return users.stream().map(u -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(u, userVO);
            return userVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean editUser(UserEditRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);

        Long userId = getLoginUser().getId();
        user.setId(userId);

        User byId = getById(userId);
        UserVO loginUser = new UserVO();
        BeanUtils.copyProperties(byId, loginUser);

        StpKit.USER.getSession().set("loginUser", loginUser);

        return this.updateById(user);
    }

    @Override
    public Boolean sendValidationCode(SendValidationCodeRequest request) {
        String mainAddress = request.getMailAddress();

        String key = VALIDATION_CODE_KEY_PREFIX + mainAddress;

        ThrowUtils.throwIf(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key)),
                ErrorCode.PARAM_ERROR, "验证码已发送, 请勿频繁操作");

        try {
            String verificationCode = RandomUtil.randomNumbers(6);

            stringRedisTemplate.opsForValue().set(key, verificationCode,
                    5, TimeUnit.MINUTES);


            String message = String.format(MAIL_HTML_TEMPLATE, verificationCode);

            mailManager.sendMail(mainAddress, "Fancy-Picture", message);
        } catch (Exception e) {
            stringRedisTemplate.delete(key);
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public UserVO getUserVO(Long id) {
        User byId = getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(byId, userVO);
        return userVO;
    }

    @Override
    public String encode(String password) {
        return DigestUtils
                .md5DigestAsHex((salt + password).getBytes());
    }
}




