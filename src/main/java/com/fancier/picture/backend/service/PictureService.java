package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.dto.PicturePageQuery;
import com.fancier.picture.backend.model.picture.dto.ReviewPictureRequest;
import com.fancier.picture.backend.model.picture.dto.UpdatePictureRequest;
import com.fancier.picture.backend.model.picture.dto.UploadPictureRequest;
import com.fancier.picture.backend.model.picture.vo.PictureTagCategory;
import com.fancier.picture.backend.model.picture.vo.PictureVO;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-17 12:54:14
*/
public interface PictureService extends IService<Picture> {



    PictureVO uploadPicture(Object inputSource, UploadPictureRequest request);

    Boolean delete(Long id);

    Boolean updatePicture(UpdatePictureRequest request);

    PictureVO getVOById(Long id);

    Page<Picture> pageQuery(PicturePageQuery pageQuery);

    Page<PictureVO> voPageQuery(PicturePageQuery pageQuery);

    Page<PictureVO> voPageQueryByCache(PicturePageQuery pageQuery);

    Boolean edit(UpdatePictureRequest request);

    PictureTagCategory listPictureTagCategory();

    Boolean review(ReviewPictureRequest request);
}
