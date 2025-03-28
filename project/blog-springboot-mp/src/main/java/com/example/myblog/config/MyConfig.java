package com.example.myblog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Value("${imagepath}")
    private String imagepath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns("/user/reg") // 排除注册接口（注册接口不拦截）
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/getuser")
                .excludePathPatterns("/user/getsess")
                .excludePathPatterns("/user/isartbyme")
                .excludePathPatterns("/getcaptcha")
                .excludePathPatterns("/art/list")
                .excludePathPatterns("/art/detail")
                .excludePathPatterns("/art/update_rcount")
                .excludePathPatterns("/comment/list")
                .excludePathPatterns("/**/*.html")
                .excludePathPatterns("/css/**")
                .excludePathPatterns("/editor.md/**")
                .excludePathPatterns("/img/**")
                .excludePathPatterns("/image/**")
                .excludePathPatterns("/js/**");

    }


    /**
     * 映射图片路径
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        registry.addResourceHandler("/image/**").addResourceLocations("file:" + imagepath);
//这段代码意思就配置一个拦截器， 如果访问路径是addResourceHandler中的filepath 这个路径 那么就 映射到访问本地的addResourceLocations
// 的参数的这个路径上，这样就可以让别人访问服务器的本地文件了，比如本地图片或者本地音乐视频什么的。
    }

    
}















