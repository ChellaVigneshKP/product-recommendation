package com.bigdata.ecom.auth.config;

import com.bigdata.ecom.auth.model.User;
import com.bigdata.ecom.auth.repository.UserRepository;
import com.bigdata.ecom.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private static final String JWT_COOKIE_NAME = "token";

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Cookie jwtCookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
        String token = jwtCookie != null ? jwtCookie.getValue() : null;
        try {
            Long userId = jwtUtil.validateToken(token);
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.get(), null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("User authenticated: " + user.get());
            } else {
                logger.warn("User not found with ID: " + userId);
            }
        } catch (Exception e) {
            logger.error("Error during JWT authentication");
        } finally {
            chain.doFilter(request, response);
        }
    }
}
