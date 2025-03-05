package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.Picture;
import com.fancier.picture.backend.model.User;
import com.fancier.picture.backend.model.picture.dto.PictureQueryRequest;
import com.fancier.picture.backend.model.picture.dto.PictureReviewRequest;
import com.fancier.picture.backend.model.picture.dto.PictureUploadByBatchRequest;
import com.fancier.picture.backend.model.picture.dto.PictureUploadRequest;
import com.fancier.picture.backend.model.picture.vo.PictureVO;

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
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

}
