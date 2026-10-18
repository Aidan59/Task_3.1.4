package ru.kata.spring.boot_security.demo.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private static final int MAX_BODY_LENGTH = 2000;
    private static final Pattern PASSWORD =
            Pattern.compile("(\"password\"\\s*:\\s*)\"(?:[^\"\\\\]|\\\\.)*\"");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_BODY_LENGTH + 1);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            log.info("{} {} user={} status={} time={}ms request={} response={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    currentUser(),
                    responseWrapper.getStatus(),
                    System.currentTimeMillis() - start,
                    printable(requestWrapper.getContentAsByteArray()),
                    printable(responseWrapper.getContentAsByteArray()));

            responseWrapper.copyBodyToResponse();
        }
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonymous" : auth.getName();
    }

    private String printable(byte[] body) {
        if (body.length == 0) {
            return "-";
        }
        String text = new String(body, StandardCharsets.UTF_8);
        text = PASSWORD.matcher(text).replaceAll("$1\"***\"");
        return text.length() > MAX_BODY_LENGTH
                ? text.substring(0, MAX_BODY_LENGTH) + "...(truncated)"
                : text;
    }
}
