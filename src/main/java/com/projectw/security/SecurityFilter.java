package com.projectw.security;

import com.projectw.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "Jwt_Filter")
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if(requestURI.equals("/health") ||
                requestURI.startsWith("/api/search") ||
                requestURI.startsWith("/api/categories") ||
                requestURI.startsWith("/search")) {
            filterChain.doFilter(request, response);
            return;
        }

        if(!requestURI.startsWith("/api/v\\d+/auth/logout") && requestURI.matches("/api/v\\d+/auth/.*")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = jwtUtil.substringToken(authorizationHeader);
            try{
                Claims claims = jwtUtil.extractClaims(jwt);
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                UserRole userRole = UserRole.of(claims.get("userRole", String.class));
                if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    AuthUser authUser = new AuthUser(Long.parseLong(userId), email, userRole);
                    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (SecurityException | MalformedJwtException e) {
                log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
            } catch (ExpiredJwtException e) {
                log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
            } catch (UnsupportedJwtException e) {
                log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
            } catch (Exception e) {
                log.error("Internal server error", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        filterChain.doFilter(request, response);
    }
}
