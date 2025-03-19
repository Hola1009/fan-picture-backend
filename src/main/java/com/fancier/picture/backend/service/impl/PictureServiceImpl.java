package com.fancier.picture.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.helper.tencentCOS.UploadPictureHelper;
import com.fancier.picture.backend.helper.tencentCOS.model.UploadPictureResult;
import com.fancier.picture.backend.mapper.PictureMapper;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.dto.UploadPictureRequest;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-17 12:54:14
*/
@Service
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    private final UploadPictureHelper uploadPictureHelper;

    private final UserServiceImpl userService;

    private final SpaceService spaceService;

    /**
     * 该接口为以下服务共用
     *  1. 上传图片到公共图库
     *  2. 上传图片到个人图库
     *  3. 根据更新图片 url
     * 如果是更新的话优先更新
     * 然后再判断是公共服务还是
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, UploadPictureRequest request) {
        Long spaceId = request.getSpaceId();
        Long pictureId = request.getId();
        Long userId = userService.getLoginUser().getId();
        Picture oldPicture;

        if (pictureId != null) { // 更新图片 url
            oldPicture = getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAM_ERROR, "图片不存在");
            // 防止 spaceId 误传
            spaceId = oldPicture.getSpaceId();
            // 如果 pictureId 存在，则说明是更新操做
            // 这一步是预见了管理员操作其他用户图片
            userId = oldPicture.getUserId();
        } else if (spaceId != null) { // 上传图片到指定空间, 需要保证空间存在
            Space space = spaceService.lambdaQuery().select(Space::getId)
                    .eq(Space::getId, spaceId).one();
            ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间不存在");
        }

        // 上传图片, 区分公共图库和其他图库
        String uploadPathPrefix = spaceId == null ? "public/" : "space/" + spaceId + "/";
        UploadPictureResult uploadPictureResult = uploadPictureHelper.uploadFile(multipartFile, uploadPathPrefix);
        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadPictureResult, picture);

        // 通用更新操作和插入操作, 不用写那么多 if 了
        picture.setSpaceId(spaceId);
        picture.setId(pictureId);
        picture.setUserId(userId);

        saveOrUpdate(picture);

        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);

        return pictureVO;
    }
}




