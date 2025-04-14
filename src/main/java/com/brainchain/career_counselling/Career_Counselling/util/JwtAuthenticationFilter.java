package com.brainchain.career_counselling.Career_Counselling.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String email = null;
        String jwtToken = null;
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            jwtToken = header.substring(BEARER_PREFIX.length());
            try {
                if (jwtUtil.validateToken(jwtToken)) {
                    email = jwtUtil.extractEmail(jwtToken);
                } else {
                    LOGGER.warn("Invalid JWT token");
                }
            } catch (ExpiredJwtException e) {
                LOGGER.warn("JWT token expired: {}", e.getMessage());
            } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                LOGGER.warn("JWT token error: {}", e.getMessage());
            }
        } else {
            LOGGER.debug("No Bearer token found in Authorization header");
        }
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, jwtUtil.getAuthorities(jwtToken));
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            LOGGER.debug("Authenticated user: {}", email);
        }
        chain.doFilter(request, response);
    }
}
