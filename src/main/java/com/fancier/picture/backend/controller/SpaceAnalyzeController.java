package com.fancier.picture.backend.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.fancier.picture.backend.auth.constant.KitType;
import com.fancier.picture.backend.auth.constant.SpacePermission;
import com.fancier.picture.backend.auth.constant.UserRole;
import com.fancier.picture.backend.common.BaseResponse;
import com.fancier.picture.backend.common.ResultUtils;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceRankAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceUserAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.vo.*;
import com.fancier.picture.backend.service.SpaceAnalyzeService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    private final SpaceAnalyzeService spaceAnalyzeService;

    @PostMapping("/usage")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceAnalyzeRequest request) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceUsageAnalyze(request));
    }

    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceAnalyzeRequest request) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceCategoryAnalyze(request));
    }

    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceAnalyzeRequest request) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceTagAnalyze(request));
    }

    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceAnalyzeRequest spaceSizeAnalyzeRequest) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest));
    }

    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest request) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceUserAnalyze(request));
    }

    @PostMapping("/rank")
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest request) {
        return ResultUtils.success(spaceAnalyzeService.getSpaceRankAnalyze(request));
    }
}
