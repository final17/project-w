package com.projectw.domain.auth.service;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.enums.TokenType;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.common.exceptions.InvalidRequestException;
import com.projectw.common.exceptions.InvalidTokenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.auth.dto.AuthRequest;
import com.projectw.domain.auth.dto.AuthRequest.Login;
import com.projectw.domain.auth.dto.AuthResponse;
import com.projectw.domain.auth.dto.AuthResponse.DuplicateCheck;
import com.projectw.domain.auth.dto.AuthResponse.Reissue;
import com.projectw.domain.auth.dto.AuthResponse.Signup;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.security.AuthUser;
import com.projectw.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;

    @Value("${ADMIN_TOKEN}")
    private String adminToken;

    /**
     * 회원 가입
     * @param request
     * @return
     */
    public SuccessResponse<Signup> signup(AuthRequest.Signup request) {
        if(!request.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$")) {
            throw new InvalidRequestException(ResponseCode.INVALID_PASSWORD);
        }

        if(request.userRole() == UserRole.ROLE_ADMIN) {
            if( !StringUtils.hasText(request.adminToken()) || !request.adminToken().equals(adminToken)) {
                throw new AccessDeniedException(ResponseCode.FORBIDDEN);
            }
        }

        String password = passwordEncoder.encode(request.password());
        String email = request.email();
        String nickname = request.nickname();


        // email 중복확인
        if (userRepository.existsByEmail(email)) {
            throw new InvalidRequestException(ResponseCode.DUPLICATE_EMAIL);
        }

        // nickname 중복확인
        if (userRepository.existsByNickname(nickname)) {
            throw new InvalidRequestException(ResponseCode.DUPLICATE_NICKNAME);
        }

        // 사용자 등록
        User user = new User(password, email, nickname, request.userRole());
        user = userRepository.save(user);

        return SuccessResponse.of(new AuthResponse.Signup(user.getId()));
    }

    /**
     * 로그인
     * @param request
     * @return
     */
    public AuthResponse.Login login(Login request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(()-> new InvalidRequestException(ResponseCode.WRONG_EMAIL_OR_PASSWORD));

        if(user.isDeleted()) {
            throw new InvalidRequestException(ResponseCode.ALREADY_DELETED_USER);
        }

        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidRequestException(ResponseCode.WRONG_PASSWORD);
        }

        // 어드민 로그인 시 어드민 토큰 검증
        if(user.getRole() == UserRole.ROLE_ADMIN) {
            if(!StringUtils.hasText(request.adminToken())){
                throw new AccessDeniedException(ResponseCode.FORBIDDEN);
            }

            if(!request.adminToken().equals(adminToken)) {
                throw new AccessDeniedException(ResponseCode.FORBIDDEN);
            }
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getRole());

        redissonClient.getBucket(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + user.getId()).set(refreshToken, Duration.ofMillis(TokenType.REFRESH.getLifeTime()));
        return new AuthResponse.Login(user, accessToken, refreshToken);
    }

    /**
     * 로그 아웃
     * @param user
     * @return
     */
    public SuccessResponse<Void> logout(AuthUser user) {
        redissonClient.getBucket(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + user.getUserId()).delete();
        return SuccessResponse.of(null);
    }

    /**
     * 액세스, 리프레쉬 토큰 재발행
     * @param refreshToken
     * @return
     */
    public Reissue reissue(String refreshToken) {

        if(refreshToken == null) {
            throw new InvalidTokenException();
        }

        // 프론트에서 붙여준 Bearer prefix 제거
        try{
            refreshToken = jwtUtil.substringToken(refreshToken);
        } catch (NullPointerException e) {
            throw new InvalidTokenException();
        }

        // 리프레쉬 토큰인지 검사
        String category = jwtUtil.getTokenCategory(refreshToken);
        if (!category.equals(TokenType.REFRESH.name())) {
            throw new InvalidTokenException();
        }

        // 토큰 만료 검사
        try{
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException();
        }


        String key = JwtUtil.REDIS_REFRESH_TOKEN_PREFIX  + jwtUtil.getUserId(refreshToken);
        // 레디스에서 리프레쉬 토큰을 가져온다.
        refreshToken = (String) redissonClient.getBucket(key).get();

        if (refreshToken == null) {
            throw new InvalidTokenException();
        }

        // redis에서 꺼내온 리프레쉬 토큰 prefix 제거
        refreshToken = jwtUtil.substringToken(refreshToken);

        // 검증이 통과되었다면 refresh 토큰으로 액세스 토큰을 발행해준다.
        Claims claims = jwtUtil.extractClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        // 새 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(userId, email, userRole);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, email, userRole);

        // TTL 새로해서
        String userIdToString = String.valueOf(userId);
        RBucket<Object> refreshBucket = redissonClient.getBucket(JwtUtil.REDIS_REFRESH_TOKEN_PREFIX + userIdToString);
        long ttl = refreshBucket.remainTimeToLive();

        if(ttl < 0) {
            throw new InvalidTokenException();
        }

        refreshBucket.set(newRefreshToken, Duration.ofMillis(ttl));

        return new Reissue(newAccessToken, newRefreshToken);
    }

    /**
     * 유저 닉네임 중복 체크
     * @param request
     * @return
     */
    public SuccessResponse<AuthResponse.DuplicateCheck> checkNickname(AuthRequest.CheckNickname request) {
        DuplicateCheck duplicateCheck = new DuplicateCheck(
            userRepository.existsByNickname(request.nickname()));

        return SuccessResponse.of(duplicateCheck);
    }

    /**
     * 유저 이메일 중복 체크
     * @param request
     * @return
     */
    public SuccessResponse<AuthResponse.DuplicateCheck> checkEmail(AuthRequest.CheckEmail request) {
        DuplicateCheck duplicateCheck = new DuplicateCheck(
            userRepository.existsByEmail(request.email()));

        return SuccessResponse.of(duplicateCheck);
    }

    /**
     * 회원탈퇴
     */
    public void deleteAccount(AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_USER));

        if(user.isDeleted()){
            throw new InvalidRequestException(ResponseCode.ALREADY_DELETED_USER);
        }

        user.delete();
    }
}
