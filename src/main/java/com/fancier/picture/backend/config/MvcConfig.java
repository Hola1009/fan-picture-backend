package com.fancier.picture.backend.config;

import com.fancier.picture.backend.auth.interceptor.SpaceAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpaceAuthInterceptor()).addPathPatterns("/**");
    }
}
