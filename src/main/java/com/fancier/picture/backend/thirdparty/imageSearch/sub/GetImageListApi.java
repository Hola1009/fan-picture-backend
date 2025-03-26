package com.fancier.picture.backend.thirdparty.imageSearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fancier.picture.backend.common.exception.BusinessException;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.thirdparty.imageSearch.model.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取360搜图搜索的图片的列表
 *
 * @author Baolong 2025年02月19 22:58
 * @version 1.0
 * @since 1.8
 */
@Slf4j
public class GetImageListApi {

	/**
	 * 获取图片列表
	 *
	 * @param imageUrl 图片地址, 在 360 库中的地址
	 * @return 图片列表对象
	 */
	public static List<ImageSearchResult> getImageList(String imageUrl, Integer start) {
		String url = "https://st.so.com/stu?a=mrecomm&start=" + start;
		Map<String, Object> formData = new HashMap<>();
		formData.put("img_url", imageUrl);
		HttpResponse response = HttpRequest.post(url)
				.form(formData)
				.timeout(5000)
				.execute();
		// 判断响应状态
		if (HttpStatus.HTTP_OK != response.getStatus()) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜图失败");
		}
		// 解析响应
		JSONObject body = JSONUtil.parseObj(response.body());
		// 处理响应结果
		if (!Integer.valueOf(0).equals(body.getInt("errno"))) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜图失败");
		}
		JSONObject data = body.getJSONObject("data");
		List<ImageSearchResult> result = data.getBeanList("result", ImageSearchResult.class);
		// 对结果进行处理, 因为返回的是分开的对象, 不是一个完整的图片路径, 这里需要自己拼接
		for (ImageSearchResult soImageSearchResult : result) {
			String prefix;
			if (StrUtil.isNotBlank(soImageSearchResult.getHttps())) {
				prefix = "https://" + soImageSearchResult.getHttps() + "/";
			} else {
				prefix = "http://" + soImageSearchResult.getHttp() + "/";
			}
			soImageSearchResult.setImgUrl(prefix + soImageSearchResult.getImgkey());
		}
		return result;
	}


}

