package com.fancier.picture.backend.config.filter;

import cn.hutool.http.ContentType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Order(1)
@Component
public class HttpRequestWrapperFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            String contentType = httpServletRequest.getContentType();

            if (ContentType.JSON.getValue().equals(contentType)) {
                chain.doFilter(new HttpRequestWrapper(httpServletRequest), response);
                return;
            }

            request.getContentType();
            chain.doFilter(request, response);
        }
    }
}
