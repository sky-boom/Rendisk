package com.wzr.rendisk.config.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 配置类
 * @author wzr
 * @date 2023-06-07 21:23
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    /** 自定义用户参数 */
    @Autowired
    private UserArgumentResolver userArgumentResolver;

    /**
     * 自定义用户参数，添加至mvc配置
     * @param resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // controller中添加 User参数的自动注入和校验。
        resolvers.add(userArgumentResolver);
    }
}
