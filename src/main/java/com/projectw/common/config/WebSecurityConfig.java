package com.projectw.common.config;

import com.projectw.common.enums.UserRole;
import com.projectw.security.SecurityFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // Spring Security 지원을 가능하게 함
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled  = true) // @Secured 사용가능하게
public class WebSecurityConfig {

    private final SecurityFilter securityFilter;
    private final CorsConfigurationSource corsConfigurationSource;


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // SessionManagementFilter, SecurityContextPersistenceFilter
            )
            .addFilterBefore(securityFilter, SecurityContextHolderAwareRequestFilter.class)
            .formLogin(AbstractHttpConfigurer::disable) // UsernamePasswordAuthenticationFilter, DefaultLoginPageGeneratingFilter 비활성화
            .anonymous(AbstractHttpConfigurer::disable) // AnonymousAuthenticationFilter 비활성화
            .httpBasic(AbstractHttpConfigurer::disable) // BasicAuthenticationFilter 비활성화
            .logout(AbstractHttpConfigurer::disable) // LogoutFilter 비활성화
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/*/auth/**").permitAll()
                .requestMatchers("/api/*/user/stores/*/waitings/connection").permitAll() // sse 연결끊으면 에러 때문에 permitAll 처리
                .requestMatchers("/auth/*/logout").authenticated()
                .requestMatchers("/api/*/user/**").hasAnyAuthority(UserRole.Authority.USER, UserRole.Authority.OWNER, UserRole.Authority.ADMIN)
                .requestMatchers("/api/*/owner/**").hasAnyAuthority(UserRole.Authority.OWNER, UserRole.Authority.ADMIN)
                .requestMatchers("/api/*/owner/**").hasAuthority(UserRole.Authority.ADMIN)
                .requestMatchers("/api/*/allergies").permitAll()
                .requestMatchers("/api/*/payment/success" , "/api/*/payment/fail").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                .requestMatchers("/search/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .anyRequest().authenticated()
            );
        http.cors(c -> {
            c.configurationSource(corsConfigurationSource);});

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
