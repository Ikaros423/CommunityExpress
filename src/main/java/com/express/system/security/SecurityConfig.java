package com.express.system.security;

import com.express.system.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/system/sysUser/login",
                                "/system/sysUser/register"
                        ).permitAll()
                        .requestMatchers(
                                "/doc.html",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/system/expressInfo/list", "/system/expressInfo/checkout")
                        .hasAnyRole("USER", "STAFF", "ADMIN")
                        .requestMatchers("/system/expressInfo/**", "/system/shelfInfo/**")
                        .hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/system/sysUser/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // 认证失败统一返回 JSON。
                        .authenticationEntryPoint((request, response, authException) -> {
                            try {
                                writeJson(response, HttpStatus.UNAUTHORIZED.value(), "未登录或登录已过期");
                            } catch (Exception e) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            try {
                                writeJson(response, HttpStatus.FORBIDDEN.value(), "无权限访问");
                            } catch (Exception e) {
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                            }
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws Exception {
        // 确保错误响应以 UTF-8 输出。
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message)));
    }
}
