package com.example.onlinejudge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Spring MVC 配置类
 * 用于配置Web MVC相关的设置，如静态资源映射、资源处理等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 添加资源处理器
     * 配置静态资源的访问路径映射，包括上传文件目录和前端静态资源
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传的文件目录：将所有 /uploads/** 的请求转发到工作目录下的 uploads 文件夹
        // 例如：请求 /uploads/image.jpg 会访问服务器上 {工作目录}/uploads/image.jpg 文件
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:" + System.getProperty("user.dir") + "/uploads/",
                        "classpath:/static/uploads/"
                );
        // 保留原有的静态资源映射
        // 将所有其他请求映射到 classpath:/static/ 目录下
        // 并添加自定义的资源解析器，用于支持前端单页应用的路由
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)
                .addResolver(new PushStateResourceResolver());
    }

    /**
     * 自定义资源解析器，用于支持前端单页应用（SPA）的路由
     * 当访问的不是静态资源时，返回 index.html，让前端路由处理
     */
    private class PushStateResourceResolver implements ResourceResolver {
        // 默认返回的首页资源
        private final Resource index = new ClassPathResource("/static/index.html");
        // 需要由后端处理的文件扩展名列表
        private final List<String> handledExtensions = Arrays.asList("html", "js", "json", "csv", "css", "png", "svg", "eot", "ttf", "woff", "appcache", "jpg", "jpeg", "gif", "ico");
        // 需要忽略的路径前缀，这些路径不会被重定向到 index.html
        private final List<String> ignoredPaths = Arrays.asList("api");

        /**
         * 解析资源
         * 根据请求路径解析对应的资源
         *
         * @param request     当前HTTP请求
         * @param requestPath 请求路径
         * @param locations   资源位置列表
         * @param chain       资源解析器链
         * @return 解析后的资源，如果找不到则返回 index.html
         */
        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            return resolve(requestPath, locations);
        }

        /**
         * 解析URL路径
         * 将资源路径解析为URL路径
         *
         * @param resourcePath 资源路径
         * @param locations    资源位置列表
         * @param chain        资源解析器链
         * @return 解析后的URL路径
         */
        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
            Resource resolvedResource = resolve(resourcePath, locations);
            if (resolvedResource == null) {
                return null;
            }
            try {
                return resolvedResource.getURL().toString();
            } catch (IOException e) {
                return resolvedResource.getFilename();
            }
        }

        /**
         * 核心解析逻辑
         * 根据请求路径和资源位置列表解析资源
         *
         * @param requestPath 请求路径
         * @param locations   资源位置列表
         * @return 解析后的资源
         */
        private Resource resolve(String requestPath, List<? extends Resource> locations) {
            // 如果是被忽略的路径（如API请求），则不处理
            if (isIgnored(requestPath)) {
                return null;
            }
            // 如果是静态资源（根据扩展名判断），则尝试查找对应的资源文件
            if (isHandled(requestPath)) {
                return locations.stream()
                        .map(loc -> createRelative(loc, requestPath))
                        .filter(resource -> resource != null && resource.exists())
                        .findFirst()
                        .orElse(null);
            }
            // 如果不是静态资源，则返回 index.html，交由前端路由处理
            return index;
        }

        /**
         * 创建相对资源
         * 根据基础资源和相对路径创建新的资源
         *
         * @param resource     基础资源
         * @param relativePath 相对路径
         * @return 创建的相对资源
         */
        private Resource createRelative(Resource resource, String relativePath) {
            try {
                return resource.createRelative(relativePath);
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * 判断路径是否被忽略
         * 检查请求路径是否以忽略列表中的前缀开头
         *
         * @param path 请求路径
         * @return 是否被忽略
         */
        private boolean isIgnored(String path) {
            return ignoredPaths.stream().anyMatch(path::startsWith)
                    || path.startsWith("uploads/");  // 额外忽略 uploads 前缀
        }

        /**
         * 判断路径是否为需要处理的静态资源
         * 根据文件扩展名判断
         *
         * @param path 请求路径
         * @return 是否为需要处理的静态资源
         */
        private boolean isHandled(String path) {
            String extension = getExtension(path);
            return handledExtensions.stream().anyMatch(ext -> ext.equals(extension));
        }

        /**
         * 获取路径的文件扩展名
         *
         * @param path 请求路径
         * @return 文件扩展名
         */
        private String getExtension(String path) {
            String extension = "";
            if (path.contains(".")) {
                extension = path.substring(path.lastIndexOf(".") + 1);
            }
            return extension;
        }
    }
}
