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
import java.util.List;

@Slf4j(topic = "Jwt_Filter")
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (validatePublicUrl(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Public 경로 처리
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        // Authorization 헤더 확인
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 없거나 올바르지 않습니다. URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtUtil.substringToken(authorizationHeader);

        try {
            Claims claims = jwtUtil.extractClaims(jwt);

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            UserRole userRole = UserRole.of(claims.get("userRole", String.class));

            // SecurityContext 설정
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AuthUser authUser = new AuthUser(Long.parseLong(userId), email, userRole);
                JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (SecurityException | MalformedJwtException e) {
            handleError(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.", e);
            return;
        } catch (ExpiredJwtException e) {
            handleError(response, HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.", e);
            return;
        } catch (UnsupportedJwtException e) {
            handleError(response, HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.", e);
            return;
        } catch (Exception e) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Public 경로 확인
     */
    private boolean isPublicPath(String requestURI) {
        List<String> publicPaths = List.of(
                "/health",
                "/api/search",
                "/api/categories",
                "/search"
        );

        String authPathRegex = "/api/v\\d+/auth/.*";

        return publicPaths.stream().anyMatch(requestURI::startsWith)
                || (requestURI.matches(authPathRegex) && !requestURI.contains("logout"));
    }

    private boolean validatePublicUrl(String url) {
        return (url.equals("/api/v1/user/stores"));
    }

    /**
     * 에러 응답 처리
     */
    private void handleError(HttpServletResponse response, int status, String message, Exception e) throws IOException {
        log.error(message, e);
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(String.format("{\"message\": \"%s\"}", message));
        response.getWriter().flush();
        SecurityContextHolder.clearContext(); // 인증 정보 초기화
    }

}
