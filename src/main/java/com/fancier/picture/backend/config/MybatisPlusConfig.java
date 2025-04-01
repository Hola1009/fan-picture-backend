package com.fancier.picture.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件（根据数据库类型选择）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // MySQL
        // 如果是其他数据库，如 Oracle、PostgreSQL，需调整 DbType
        return interceptor;
    }
}