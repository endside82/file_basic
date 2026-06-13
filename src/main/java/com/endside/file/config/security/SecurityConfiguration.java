package com.endside.file.config.security;

import com.endside.file.user.service.JwtAuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;


@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${admin.verification-key}")
    private String adminKey;

    private final JwtAuthenticationService jwtAuthenticationService;

    public SecurityConfiguration(JwtAuthenticationService jwtAuthenticationService) {
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @Bean
    protected SecurityFilterChain web(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        http.authenticationManager(authenticationManagerBuilder.eraseCredentials(true).build());

        http
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // add jwt filters (1. authentication, 2. authorization)
                .addFilter(new JwtAuthorizationFilter(authenticationManagerBuilder.getObject(), jwtAuthenticationService, adminKey, secret));
        http.authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/", "/error", "/file/error"
                                , "/hello", "/file/mng/hello", "/file/mng/hello/log" // MAIN
                                , "/file/mng/v1/download/public/resource"
                                , "/file/mng/v1/white/list"
                                , "/file/mng/v1/{adminKey}/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}
