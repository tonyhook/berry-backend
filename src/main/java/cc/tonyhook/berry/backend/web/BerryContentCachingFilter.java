package cc.tonyhook.berry.backend.web;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BerryContentCachingFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        BerryCachedBodyHttpServletRequest cachedBodyHttpServletRequest = new BerryCachedBodyHttpServletRequest(request);
        BerryCachedBodyHttpServletResponse cachedBodyHttpServletResponse = new BerryCachedBodyHttpServletResponse(response);
        filterChain.doFilter(cachedBodyHttpServletRequest, cachedBodyHttpServletResponse);
    }

}
