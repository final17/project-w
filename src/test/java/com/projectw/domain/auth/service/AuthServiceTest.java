package com.projectw.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.projectw.common.dto.SuccessResponse;
import com.projectw.common.enums.UserRole;
import com.projectw.common.exceptions.AccessDeniedException;
import com.projectw.common.exceptions.InvalidRequestException;
import com.projectw.domain.auth.dto.AuthRequest;
import com.projectw.domain.auth.dto.AuthResponse.Signup;
import com.projectw.domain.user.entitiy.User;
import com.projectw.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Spy
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Nested
    class 회원가입 {
        @Test
        public void 회원가입_성공() throws Exception {
            // given
            AuthRequest.Signup authRequest = new AuthRequest.Signup("username", "AAbb1234!", "email", "nn", null, UserRole.ROLE_USER);
            given(userRepository.existsByUsername(any())).willReturn(false);
            given(userRepository.existsByNickname(any())).willReturn(false);
            given(userRepository.existsByEmail(any())).willReturn(false);
            User user = new User("username", "AAbb1234!", "email", "nn", UserRole.ROLE_USER);
            ReflectionTestUtils.setField(user, "id", 1L);
            given(userRepository.save(any())).willReturn(user);
            // when
            SuccessResponse<Signup> signup = authService.signup(authRequest);

            // then
            assertThat(signup.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(signup.getData().userId()).isEqualTo(1L);
        }

        @Test
        public void 어드민_회원가입_성공() throws Exception {
            // given
            AuthRequest.Signup authRequest = new AuthRequest.Signup("username", "AAbb1234!", "email", "nn", "1", UserRole.ROLE_USER);
            given(userRepository.existsByUsername(any())).willReturn(false);
            given(userRepository.existsByNickname(any())).willReturn(false);
            given(userRepository.existsByEmail(any())).willReturn(false);
            User user = new User("username", "AAbb1234!", "email", "nn", UserRole.ROLE_USER);
            ReflectionTestUtils.setField(user, "id", 1L);
            given(userRepository.save(any())).willReturn(user);
            ReflectionTestUtils.setField(authService, "adminToken", "1");
            // when
            SuccessResponse<Signup> signup = authService.signup(authRequest);

            // then
            assertThat(signup.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(signup.getData().userId()).isEqualTo(1L);
        }
        @Test
        public void 회원가입_비밀번호_조건에_부합하지않을_때() throws Exception {
            // given
            AuthRequest.Signup authRequest = new AuthRequest.Signup("username", "aab234!", "email", "nn", null, UserRole.ROLE_USER);
            // when then
            assertThatThrownBy(() -> authService.signup(authRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("비밀번호는 대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함해야하며 최소 8글자 이상이어야 합니다.");
        }

        @Test
        public void 유저역할이_관리자인데_어드민_토큰이_없거나_다를때() throws Exception {
            // given
            AuthRequest.Signup authRequest = new AuthRequest.Signup("username", "AAbb1234!", "email", "nn", null, UserRole.ROLE_ADMIN);
            // when then
            assertThatThrownBy(() -> authService.signup(authRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("접근 권한이 없습니다.");

            AuthRequest.Signup authRequest2 = new AuthRequest.Signup("username", "AAbb1234!", "email", "nn",
                "test", UserRole.ROLE_ADMIN);
            assertThatThrownBy(() -> authService.signup(authRequest2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("접근 권한이 없습니다.");
        }

        @Test
        public void 중복된_유저네임이_있을_때() throws Exception {
            // given
            AuthRequest.Signup authRequest = new AuthRequest.Signup("username", "AAbb1234!", "email", "nn", null, UserRole.ROLE_USER);
            given(userRepository.existsByUsername(any())).willReturn(true);
            // when then
            assertThatThrownBy(() -> authService.signup(authRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 아이디입니다.");
        }

        @Test
        public void 중복된_이메일이_있을_때() throws Exception {
            // given
            AuthRequest.Signup authRequest = new AuthRequest.Signup("username", "AAbb1234!", "email", "nn", null, UserRole.ROLE_USER);
            given(userRepository.existsByUsername(any())).willReturn(false);
            given(userRepository.existsByEmail(any())).willReturn(true);
            // when then
            assertThatThrownBy(() -> authService.signup(authRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
        }

        @Test
        public void 중복된_닉네임이_있을_때() throws Exception {
            // given
            AuthRequest.Signup authRequest = new AuthRequest.Signup("username", "AAbb1234!", "email", "nn", null, UserRole.ROLE_USER);
            given(userRepository.existsByUsername(any())).willReturn(false);
            given(userRepository.existsByEmail(any())).willReturn(false);
            given(userRepository.existsByNickname(any())).willReturn(true);
            // when then
            assertThatThrownBy(() -> authService.signup(authRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");
        }
    }

    @Nested
    class 로그인 {

    }

}