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
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.dto.*;
import com.fancier.picture.backend.model.picture.vo.PictureTagCategory;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.thirdparty.aliyunai.AliYunAiApi;
import com.fancier.picture.backend.thirdparty.aliyunai.model.CreateOutPaintingTaskResponse;
import com.fancier.picture.backend.thirdparty.aliyunai.model.QueryOutPaintingTaskResponse;
import com.fancier.picture.backend.thirdparty.imageSearch.model.ImageSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/picture")
public class PictureController {

    private final PictureService pictureService;

    private final AliYunAiApi aliYunAiApi;

    @PostMapping("/upload")
    @SaCheckLogin(type = KitType.USER)
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_EDIT)
    public BaseResponse<PictureVO> upload(@RequestPart("file") MultipartFile multipartFile,
                               UploadPictureRequest request) {
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, request);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/url")
    @SaCheckLogin(type = KitType.USER)
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_EDIT)
    public BaseResponse<PictureVO> uploadByUrl(@RequestBody UploadPictureRequest request) {
        String fileUrl = request.getFileUrl();
        return ResultUtils.success(pictureService.uploadPicture(fileUrl, request));
    }
    @PostMapping("/delete")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_EDIT)
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@Validated @RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(pictureService.delete(deleteRequest.getId()));
    }

    @PostMapping("/update")
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@Validated @RequestBody UpdatePictureRequest request) {
        return ResultUtils.success(pictureService.updatePicture(request));
    }

    @PostMapping("/get")
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<Picture> get(@NotNull Long id) {
        Picture byId = pictureService.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAM_ERROR, "图片不存在");
        return ResultUtils.success(pictureService.getById(id));
    }

    @GetMapping("/get/vo")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_VIEW)
    public BaseResponse<PictureVO> getVO(@NotNull Long id) {
        return ResultUtils.success(pictureService.getVOById(id));
    }
    @PostMapping("/list/page")
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> picturePageQuery(@RequestBody PicturePageQuery pageQueryRequest) {
        return ResultUtils.success(pictureService.pageQuery(pageQueryRequest));
    }

    @PostMapping("list/page/vo")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_VIEW)
    public BaseResponse<Page<PictureVO>> pictureVOPageQuery(@RequestBody PicturePageQuery pageQuery) {
        return ResultUtils.success(pictureService.voPageQuery(pageQuery));
    }


    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> pictureVOPageQueryByCache(@RequestBody PicturePageQuery pageQuery) {
        return ResultUtils.success(pictureService.voPageQueryByCache(pageQuery));
    }

    @PostMapping("/edit")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_EDIT)
    public BaseResponse<Boolean> edit(@RequestBody @Validated UpdatePictureRequest request) {
        return ResultUtils.success(pictureService.edit(request));
    }


    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        return ResultUtils.success(pictureService.listPictureTagCategory());
    }
    @PostMapping("/review")
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPicture(@RequestBody @Validated ReviewPictureRequest request) {
        return ResultUtils.success(pictureService.review(request));
    }

    @PostMapping("/upload/batch")
    @SaCheckRole(type = KitType.USER, value = UserRole.ADMIN_ROLE)
    public BaseResponse<Integer> batchUpload(@RequestBody @Validated BatchUploadPictureRequest request) throws IOException {
        return ResultUtils.success(pictureService.batchUpload(request));
    }

    @PostMapping("/edit/batch")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_EDIT)
    public BaseResponse<Boolean> batchEdit(@RequestBody BatchEditPictureRequest request) {
        return ResultUtils.success(pictureService.batchEdit(request));
    }

    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchWithPicture(@RequestBody @Validated SearchPictureByPictureRequest request) {
        return ResultUtils.success(pictureService.searchWithPicture(request));
    }

    @PostMapping("/search/color")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchByColor(SearchPictureByColorRequest request) {
        return ResultUtils.success(pictureService.searchByColor(request));
    }

    @PostMapping("/out_painting/create_task")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody @Validated CreatePictureOutPaintingTaskRequest request) {
        return ResultUtils.success(pictureService.createPictureOutPaintingTask(request));
    }

    @GetMapping("/out_painting/get_task")
    public BaseResponse<QueryOutPaintingTaskResponse> getPictureOutPaintingTask(@NotNull String taskId) {
        return ResultUtils.success(aliYunAiApi.queryOutPaintingTaskResponse(taskId));
    }

    @PostMapping("/like")
    @SaCheckLogin(type = KitType.USER)
    public BaseResponse<?> pictureLike(@RequestBody @Validated PictureLikeRequest request) {
        pictureService.pictureLike(request);
        return ResultUtils.success(true);
    }

}
