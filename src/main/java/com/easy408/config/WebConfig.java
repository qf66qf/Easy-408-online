package com.easy408.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectRoot = System.getProperty("user.dir");
        
        // 1. 映射上传目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + projectRoot + "/uploads/");

        // 2. SPA 核心配置：将静态资源和前端路由映射到 index.html
        // 关键修复：增加了 "file:./" 和 "file:../" 以支持在开发环境中直接读取根目录的 index.html
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "file:./", "file:../")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // A. 排除 API 和 上传路径，避免掩盖后端真实的 404 错误（防止 JSON 解析报错）
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("uploads/")) {
                            return null;
                        }

                        // B. 尝试查找具体文件 (js, css, png 等)
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // C. SPA 降级策略：
                        // 如果请求的资源不存在（例如访问 /dashboard），且当前位置存在 index.html，则返回 index.html
                        Resource index = location.createRelative("index.html");
                        if (index.exists() && index.isReadable()) {
                            return index;
                        }
                        
                        return null;
                    }
                });
    }
}