package com.zombie.graphql.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * React 대시보드 연동을 위한 CORS 설정
 *
 * 허용 origin:
 * - http://localhost:3000 : React 로컬 개발 서버
 * - http://localhost:8090 : Spring Cloud Gateway
 */
@Configuration
class CorsConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:3000",  // React 개발 서버
                "http://localhost:8090",  // Gateway
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
}
