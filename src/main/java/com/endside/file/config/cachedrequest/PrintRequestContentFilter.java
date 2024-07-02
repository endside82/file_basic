package com.endside.file.config.cachedrequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@Profile({"default","dev"})
@WebFilter(filterName = "printRequestContentFilter", urlPatterns = "/file/*")
public class PrintRequestContentFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String requestUrl = httpServletRequest.getRequestURI();
        if(!requestUrl.startsWith("/hello")) {
            String authorization = httpServletRequest.getHeader("Authorization");
            log.debug("authorization is: " + authorization);
            log.debug("Request URL is: " + requestUrl);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}