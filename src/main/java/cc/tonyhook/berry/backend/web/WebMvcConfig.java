package cc.tonyhook.berry.backend.web;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private BerryLoggerInterceptor loggerInterceptor;

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedHeaders("*")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
            .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(3600)
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                public Resource getResource(String resourcePath, Resource location) throws IOException {
                    Resource requestedResource = location.createRelative(resourcePath);
                    return requestedResource.exists() && requestedResource.isReadable()
                        ? requestedResource
                        : new ClassPathResource("/static/index.html");
                }
            });
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggerInterceptor)
            .addPathPatterns("/api/managed/**");
    }

}
