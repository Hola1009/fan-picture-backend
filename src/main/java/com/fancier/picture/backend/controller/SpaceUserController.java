package com.fancier.picture.backend.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fancier.picture.backend.auth.constant.KitType;
import com.fancier.picture.backend.auth.constant.SpacePermission;
import com.fancier.picture.backend.common.BaseResponse;
import com.fancier.picture.backend.common.DeleteRequest;
import com.fancier.picture.backend.common.ResultUtils;
import com.fancier.picture.backend.model.spaceUser.SpaceUser;
import com.fancier.picture.backend.model.spaceUser.dto.AddSpaceUserRequest;
import com.fancier.picture.backend.model.spaceUser.dto.SpaceUserEditRequest;
import com.fancier.picture.backend.model.spaceUser.dto.SpaceUserQueryRequest;
import com.fancier.picture.backend.model.spaceUser.vo.SpaceUserVO;
import com.fancier.picture.backend.service.SpaceUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/spaceUser")
public class SpaceUserController {

    private final SpaceUserService spaceUserService;
    @PostMapping("/add")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> add(@Validated @RequestBody AddSpaceUserRequest request) {
        return ResultUtils.success(spaceUserService.add(request));
    }

    @PostMapping("/delete")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    private BaseResponse<Boolean> delete(@Validated @RequestBody DeleteRequest request) {
        return ResultUtils.success(spaceUserService.removeById(request.getId()));
    }

    /**
     * 查询某个成员在某个空间的信息
     */
    @PostMapping("/get")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@Validated @RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 参数校验
        return ResultUtils.success(spaceUserService.getSpaceUser(spaceUserQueryRequest));
    }

    /**
     * 查询成员信息列表
     */
    @PostMapping("/list")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest request) {
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(request));
    }

    @PostMapping("/edit")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        return ResultUtils.success(spaceUserService.edit(spaceUserEditRequest));
    }

    /**
     * 查询我加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace() {

        return ResultUtils.success(spaceUserService.getMy());
    }

}
