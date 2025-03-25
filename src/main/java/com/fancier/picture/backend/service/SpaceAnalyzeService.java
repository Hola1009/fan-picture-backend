package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceRankAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceUserAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.vo.*;

import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public interface SpaceAnalyzeService extends IService<Space> {

    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceAnalyzeRequest request);

    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceAnalyzeRequest request);

    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceAnalyzeRequest request);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceAnalyzeRequest spaceSizeAnalyzeRequest);

    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest request);

    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest request);
}

