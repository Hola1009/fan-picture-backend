package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancier.picture.backend.exception.ErrorCode;
import com.fancier.picture.backend.model.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.User;
import com.fancier.picture.backend.model.picture.dto.PictureQueryRequest;
import com.fancier.picture.backend.model.picture.dto.PictureUploadRequest;
import com.fancier.picture.backend.model.picture.dto.UploadPictureResult;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
import com.fancier.picture.backend.util.ThrowUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-05 14:51:06
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);
}
