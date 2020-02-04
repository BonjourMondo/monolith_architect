package com.imooc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Author: leesanghyuk
 * Date: 2020-02-04 16:24
 * Description:
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //实现静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //由于之前没有配置静态资源映射，swagger2访问的时候自动配置，现在需要手动配置了。
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/") //映射swagger2
                .addResourceLocations("file:/Users/leesanghyuk/Downloads/");//上传的文件映射地址

    }
}
