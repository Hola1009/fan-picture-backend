package com.fancier.picture.backend.auth;


import cn.dev33.satoken.stp.StpInterface;
import com.fancier.picture.backend.auth.constant.KitType;
import com.fancier.picture.backend.auth.constant.SpaceUserRole;
import com.fancier.picture.backend.auth.constant.StpKit;
import com.fancier.picture.backend.auth.constant.UserRole;
import com.fancier.picture.backend.auth.helper.SpaceAuthHolder;
import com.fancier.picture.backend.auth.model.SpaceAuthContext;
import com.fancier.picture.backend.auth.model.SpaceRole;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.service.SpaceUserService;
import com.fancier.picture.backend.util.JsonFileParserUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    private static final Map<String , List<String>> spaceRolePermissionsMap;

    static {
        List<SpaceRole> spaceRoles = JsonFileParserUtil.parse2ListFormResource(SpaceRole.class, "biz/spaceUserRoles.json");
        spaceRolePermissionsMap = spaceRoles.stream().collect(Collectors.toMap(SpaceRole::getKey, SpaceRole::getPermissions));
    }


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
        LoginUserVO loginUserVO = (LoginUserVO) StpKit.USER.getSession().getLoginId();
        String userRole = loginUserVO.getUserRole();
        return Collections.singletonList(userRole);
    }

    private List<String> getSpacePermissionList() {
        SpaceAuthContext spaceAuthContext = SpaceAuthHolder.get();
        String servletPath = spaceAuthContext.getServletPath();
        if (servletPath.startsWith("/picture")) {
            return handlePicturePath(spaceAuthContext);
        }

        return Collections.emptyList();
    }

    /**
     * 图片模块需要校验权限的功能如下
     * 1. 上传图片到指定图库
     * 2. 更改指定图库中图片的地址
     * 3. 删除图片
     * 4. 根据 id 获取图片 VO (未在空间中的用户不能查看)
     * 5. 编辑图片
     */
    List<String> handlePicturePath(SpaceAuthContext spaceAuthContext) {
        // 没获取到就返回一个字段全为空的对象防止空指针异常
        LoginUserVO loginUser = (LoginUserVO) StpKit.SPACE.getSession().getLoginId();

        Long userId = loginUser.getId();
        Long pictureId = spaceAuthContext.getId();
        Long spaceId = spaceAuthContext.getSpaceId();

        // 用户为管理员返回管理员权限
        if (UserRole.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return spaceRolePermissionsMap.get(SpaceUserRole.ADMIN);
        }

        // 普通用户操作公共图库
        if (spaceId == null && pictureId == null) {
            return spaceRolePermissionsMap.get(SpaceUserRole.EDITOR);
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
            return spaceRolePermissionsMap.getOrDefault(spaceRole, Collections.emptyList());
        }

        // 走到这里 space 为 null pictureId 不为 null, 表示是操作公共图库的图片
        // 如果是共有图库的图片用户者则享受编辑权限
        if (userId != null && userId.equals(ownerId)) {
            return spaceRolePermissionsMap.get(SpaceUserRole.EDITOR);
        }

        return spaceRolePermissionsMap.get(SpaceUserRole.VIEWER);
    }

}
