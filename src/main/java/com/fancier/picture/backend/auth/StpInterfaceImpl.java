package com.fancier.picture.backend.auth;


import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.ObjectUtil;
import com.fancier.picture.backend.auth.constant.KitType;
import com.fancier.picture.backend.auth.constant.SpaceUserRole;
import com.fancier.picture.backend.auth.constant.StpKit;
import com.fancier.picture.backend.auth.constant.UserRole;
import com.fancier.picture.backend.auth.helper.SpaceAuthHolder;
import com.fancier.picture.backend.auth.model.SpaceAuthContext;
import com.fancier.picture.backend.auth.model.SpaceUserAuth;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.spaceUser.SpaceUser;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.service.SpaceUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {


    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceUserService spaceUserService;



    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {

        if (loginType.equals(KitType.SPACE)) {
            return getSpacePermissionList();
        }

        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginType.equals(KitType.USER)) {
            return getUserRoleList();
        }
        return Collections.emptyList();
    }

    private List<String> getUserRoleList() {
        UserVO loginUserVO = (UserVO) StpKit.USER.getSession().getLoginId();
        String userRole = loginUserVO.getUserRole();
        return Collections.singletonList(userRole);
    }


    /**
     * <h1>核心方法</h1>
     */

    private List<String> getSpacePermissionList() {
        SpaceAuthContext spaceAuthContext = SpaceAuthHolder.get();
        String servletPath = spaceAuthContext.getServletPath();
        if (servletPath.startsWith("/picture")) {
            return handlePicturePath(spaceAuthContext);
        } else if (servletPath.startsWith("/space")) {
            return handleSpacePath(spaceAuthContext);
        } else if (servletPath.startsWith("/spaceUser")) {
            return handleSpaceUserPath(spaceAuthContext);
        }

        return Collections.emptyList();
    }





    /**
     * 图片模块需要校验权限的功能如下
     * <ol>1. 上传图片到指定图库</ol>
     * <ol>2. 更改指定图库中图片的地址</ol>
     * <ol>3. 删除图片</ol>
     * <ol>4. 根据 id 获取图片 VO (未在空间中的用户不能查看)</ol>
     * <ol>5. 编辑图片</ol>
     */
    List<String> handlePicturePath(SpaceAuthContext spaceAuthContext) {
        // 没获取到就返回一个字段全为空的对象防止空指针异常
        UserVO loginUser = (UserVO) StpKit.SPACE.getSession().getLoginId();

        Long userId = loginUser.getId();
        Long pictureId = spaceAuthContext.getId();
        Long spaceId = spaceAuthContext.getSpaceId();

        // 用户为管理员返回管理员权限
        if (UserRole.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return SpaceUserAuth.getPermissionsByRole(SpaceUserRole.ADMIN);
        }

        // 普通用户操作公共图库
        if (spaceId == null && pictureId == null) {
            return SpaceUserAuth.getPermissionsByRole(SpaceUserRole.EDITOR);
        }

        Picture picture;
        Long ownerId = null;
        // 这一步主要是为了获取 spaceId
        if (pictureId != null) {
            picture = pictureService.lambdaQuery().select(Picture::getSpaceId, Picture::getUserId)
                    .eq(Picture::getId, pictureId).one();

            ThrowUtils.throwIf(picture == null, ErrorCode.PARAM_ERROR, "图片不存在");

            // 防止空间 id 误传
            spaceId = picture.getSpaceId();
            ownerId = picture.getUserId();
        }

        // 优先从空间里返回权限
        if (spaceId != null) {
            String spaceRole = spaceUserService.getSpaceRole(userId, spaceId);
            return SpaceUserAuth.getPermissionsByRole(spaceRole);
        }

        // 走到这里 space 为 null pictureId 不为 null, 表示是操作公共图库的图片
        // 如果是共有图库的图片用户者则享受编辑权限
        if (userId != null && userId.equals(ownerId)) {
            return SpaceUserAuth.getPermissionsByRole(SpaceUserRole.EDITOR);
        }

        return SpaceUserAuth.getPermissionsByRole(SpaceUserRole.VIEWER);
    }

    /**
     * 空间模块需要权限的操作有
     * <ol>1. 删除空间</ol>
     * <ol>2. 编辑空间</ol>
     * 都只需要拿到空间 id 就可以了
     */
    List<String> handleSpacePath(SpaceAuthContext spaceAuthContext) {
        UserVO loginUser = (UserVO) StpKit.SPACE.getSession().getLoginId();
        Long userId = loginUser.getId();
        // 系统如果是管理员的话
        if(UserRole.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return SpaceUserAuth.getPermissionsByRole(SpaceUserRole.ADMIN);
        }

        Long spaceId = spaceAuthContext.getId();
        // 对于 spaceAnalyze 模块不会传 id, 只会传 spaceId
        if (spaceId == null) {
            spaceId = spaceAuthContext.getSpaceId();
        }
        String spaceRole = spaceUserService.getSpaceRole(userId, spaceId);

        return SpaceUserAuth.getPermissionsByRole(spaceRole);
    }

    /**
     * 需要进行鉴权的操作
     * <ol>1. 添加用户 会传 spaceId</ol>
     * <ol>2. 删除用户 会传 spaceUserId</ol>
     * <ol>3. 查询用户列表 会传 spaceId</ol>
     * <ol>4. 编辑用户 会传 spaceUserId</ol>
     */
    List<String> handleSpaceUserPath(SpaceAuthContext spaceAuthContext) {
        UserVO loginUser = (UserVO) StpKit.SPACE.getSession().getLoginId();
        Long userId = loginUser.getId();

        if(UserRole.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return SpaceUserAuth.getPermissionsByRole(SpaceUserRole.ADMIN);
        }

        Long spaceUserId = spaceAuthContext.getId();
        Long spaceId = spaceAuthContext.getSpaceId();

        // 如果没有传 spaceId 则通过 spaceUserId 来获取
        if (ObjectUtil.isEmpty(spaceId) && ObjectUtil.isEmpty(spaceId)) {
            SpaceUser byId = spaceUserService.getById(spaceUserId);
            ThrowUtils.throwIf(byId == null, ErrorCode.PARAM_ERROR, "空间用户 id 非法");
            spaceId = byId.getSpaceId();
        }

        // 根据 loginUserId 和 spaceId 来获取 权限
        if (ObjectUtil.isNotEmpty(spaceId)) {
            String spaceRole = spaceUserService.getSpaceRole(userId, spaceId);
            return SpaceUserAuth.getPermissionsByRole(spaceRole);
        }

        return Collections.emptyList();
    }
}
