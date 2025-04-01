package com.fancier.picture.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancier.picture.backend.auth.constant.KitType;
import com.fancier.picture.backend.auth.constant.SpacePermission;
import com.fancier.picture.backend.auth.constant.UserRole;
import com.fancier.picture.backend.common.BaseResponse;
import com.fancier.picture.backend.common.DeleteRequest;
import com.fancier.picture.backend.common.ResultUtils;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.space.constant.SpaceLevelEnum;
import com.fancier.picture.backend.model.space.dto.AddSpaceRequest;
import com.fancier.picture.backend.model.space.dto.EditSpaceRequest;
import com.fancier.picture.backend.model.space.dto.SpacePageQuery;
import com.fancier.picture.backend.model.space.dto.UpdateSpaceRequest;
import com.fancier.picture.backend.model.space.vo.SpaceLevel;
import com.fancier.picture.backend.model.space.vo.SpaceVO;
import com.fancier.picture.backend.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/space")
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping("/add")
    @SaCheckLogin(type = KitType.USER)
    public BaseResponse<Long> add(@RequestBody @Validated AddSpaceRequest request) {
        return ResultUtils.success(spaceService.addSpace(request));
    }

    @GetMapping("delete")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> delete(@Validated @RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(spaceService.removeById(deleteRequest.getId()));
    }

    @PostMapping("/update")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> update(@Validated @RequestBody UpdateSpaceRequest request) {
        return ResultUtils.success(spaceService.updateSpace(request));
    }

    @GetMapping("/get")
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<Space> getDetailById(@NotNull Long id) {
        return ResultUtils.success(spaceService.getById(id));
    }

    @GetMapping("/get/vo")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_VIEW)
    public BaseResponse<SpaceVO> getDetailVoById(@NotNull Long id) {
        return ResultUtils.success(spaceService.getDetailVOById(id));
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<Space>> pageQuery(@RequestBody SpacePageQuery pageQuery) {
        return ResultUtils.success(spaceService.pageQuery(pageQuery));
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> pageQueryVO(@RequestBody SpacePageQuery pageQuery) {
        return ResultUtils.success(spaceService.pageQueryVO(pageQuery));
    }

    @PostMapping("/edit")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> edit(@RequestBody @Validated EditSpaceRequest request) {
        return ResultUtils.success(spaceService.edit(request));
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> getSpaceLevelList() {
        return ResultUtils.success(
                Arrays.stream(SpaceLevelEnum.values()).map(e -> {
                    SpaceLevel spaceLevel = new SpaceLevel();
                    BeanUtils.copyProperties(e, spaceLevel);
                    return spaceLevel;
                }).collect(Collectors.toList()));
    }

}
