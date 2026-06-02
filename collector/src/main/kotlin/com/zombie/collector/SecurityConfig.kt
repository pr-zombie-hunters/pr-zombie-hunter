package com.zombie.collector

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/webhook/github").permitAll() // Webhook은 인증 없이 허용
                    .anyRequest().authenticated()                   // 나머지는 로그인 필요
            }
            .oauth2Login { oauth2 ->
                oauth2.defaultSuccessUrl("/", true)                // 로그인 성공 시 이동할 경로
            }
            .csrf { csrf ->
                csrf.ignoringRequestMatchers("/webhook/github")    // Webhook은 CSRF 검증 제외
            }
        return http.build()
    }
}