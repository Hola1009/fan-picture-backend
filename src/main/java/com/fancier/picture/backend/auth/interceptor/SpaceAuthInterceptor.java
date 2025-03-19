package com.fancier.picture.backend.auth.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.fancier.picture.backend.auth.helper.SpaceAuthHolder;
import com.fancier.picture.backend.auth.model.SpaceAuthContext;
import lombok.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public class SpaceAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        SpaceAuthContext spaceAuthContext;
        String contentType = request.getContentType();

        //
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            spaceAuthContext = JSONUtil.toBean(body, SpaceAuthContext.class);

            SpaceAuthHolder.set(spaceAuthContext);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            spaceAuthContext = BeanUtil.toBean(paramMap, SpaceAuthContext.class);
        }

        String servletPath = request.getServletPath();
        spaceAuthContext.setServletPath(servletPath);

        SpaceAuthHolder.set(spaceAuthContext);
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                           @NonNull Object handler, ModelAndView modelAndView) throws Exception {

        SpaceAuthHolder.clear();
    }
}
