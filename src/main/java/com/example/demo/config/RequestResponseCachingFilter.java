package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

public class RequestResponseCachingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Wrap để đọc body nhiều lần
        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(httpResponse);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            wrappedResponse.copyBodyToResponse();
        }
    }
}
