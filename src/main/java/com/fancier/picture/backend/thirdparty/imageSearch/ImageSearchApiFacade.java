package com.fancier.picture.backend.thirdparty.imageSearch;

import com.fancier.picture.backend.thirdparty.imageSearch.model.ImageSearchResult;
import com.fancier.picture.backend.thirdparty.imageSearch.sub.GetImageListApi;
import com.fancier.picture.backend.thirdparty.imageSearch.sub.GetImageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 360搜图图片搜索接口
 * <p>
 * 这里用了 门面模式
 */
@Slf4j
public class ImageSearchApiFacade {

	/**
	 * 搜索图片
	 *
	 * @param imageUrl 需要以图搜图的图片地址
	 * @param start    开始下表
	 * @return 图片搜索结果列表
	 */
	public static List<ImageSearchResult> searchImage(String imageUrl, Integer start) {
		String soImageUrl = GetImageUrlApi.getSoImageUrl(imageUrl);

		return GetImageListApi.getImageList(soImageUrl, start);
	}

}
