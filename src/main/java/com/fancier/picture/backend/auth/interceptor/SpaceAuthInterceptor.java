package com.fancier.picture.backend.auth.interceptor;

import cn.hutool.http.ContentType;
import com.fancier.picture.backend.auth.helper.SpaceAuthHolder;
import com.fancier.picture.backend.auth.model.SpaceAuthContext;
import com.fancier.picture.backend.config.filter.HttpRequestWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public class SpaceAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        String contentType = request.getContentType();
        if (ContentType.JSON.getValue().equals(contentType)) {
            HttpRequestWrapper requestWrapper = (HttpRequestWrapper) request;
            String body = requestWrapper.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            SpaceAuthContext spaceAuthContext = objectMapper.readValue(body, SpaceAuthContext.class);

            String servletPath = request.getServletPath();

            spaceAuthContext.setServletPath(servletPath);

            SpaceAuthHolder.set(spaceAuthContext);
        }

        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                           @NonNull Object handler, ModelAndView modelAndView) throws Exception {

        SpaceAuthHolder.clear();
    }
}
