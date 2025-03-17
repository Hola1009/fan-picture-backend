package com.fancier.picture.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-17 12:54:14
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




