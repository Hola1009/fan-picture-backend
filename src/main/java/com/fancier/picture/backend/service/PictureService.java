package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.dto.*;
import com.fancier.picture.backend.model.picture.vo.PictureTagCategory;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
import com.fancier.picture.backend.thirdparty.aliyunai.model.CreateOutPaintingTaskResponse;
import com.fancier.picture.backend.thirdparty.imageSearch.model.ImageSearchResult;

import java.io.IOException;
import java.util.List;

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

    Integer batchUpload(BatchUploadPictureRequest request) throws IOException;

    Boolean batchEdit(BatchEditPictureRequest request);

    List<ImageSearchResult> searchWithPicture(SearchPictureByPictureRequest request);

    List<PictureVO> searchByColor(SearchPictureByColorRequest request);

    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest request);
}
