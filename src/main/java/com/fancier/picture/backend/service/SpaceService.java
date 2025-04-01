package com.fancier.picture.backend.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.space.dto.AddSpaceRequest;
import com.fancier.picture.backend.model.space.dto.EditSpaceRequest;
import com.fancier.picture.backend.model.space.dto.SpacePageQuery;
import com.fancier.picture.backend.model.space.dto.UpdateSpaceRequest;
import com.fancier.picture.backend.model.space.vo.SpaceVO;

/**
* @author Fanfan
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-03-18 11:12:49
*/
public interface SpaceService extends IService<Space> {

    Long addSpace(AddSpaceRequest request);

    Boolean updateSpace(UpdateSpaceRequest request);

    Page<Space> pageQuery(SpacePageQuery pageQuery);

    Page<SpaceVO> pageQueryVO(SpacePageQuery pageQuery);

    Boolean edit(EditSpaceRequest request);

    SpaceVO getDetailVOById(Long id);
}
