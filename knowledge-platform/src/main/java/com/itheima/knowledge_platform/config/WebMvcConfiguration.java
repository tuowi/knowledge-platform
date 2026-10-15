package com.itheima.knowledge_platform.config;

import com.itheima.knowledge_platform.interceptor.JwtTokenAdminInterceptor;
import com.itheima.knowledge_platform.interceptor.JwtTokenUserInterceptor;
import com.itheima.knowledge_platform.properties.FilePathProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private FilePathProperties filePathProperties;

    /**
     * 注册自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        // 管理端拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login");  // 登录接口放行

        // 用户端拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns(
                        "/user/**",
                        "/ai/team/getTeamMemberInfo",
                        "/ai/document/getDocumentInfo"
                )
                .excludePathPatterns(
                        "/user/auth/login",     // 登录
                        "/user/auth/register",  // 注册
                        "/user/documents/public",  // 公开文档（可选）
                        "/user/teams/public",    // 公开团队列表（可选）
                        "/avatars/**"             // 只加这一行！图片回显放行
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + filePathProperties.getAvatarPath());
    }
}