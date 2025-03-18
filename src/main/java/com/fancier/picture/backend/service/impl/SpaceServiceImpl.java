package com.fancier.picture.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.mapper.SpaceMapper;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.service.SpaceService;
import org.springframework.stereotype.Service;

/**
* @author Fanfan
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-03-18 11:12:49
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

}




