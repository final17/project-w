package com.projectw.domain.auth.dto;

public sealed interface KakaoAuthResponse permits KakaoAuthResponse.KakaoAccount {

    record KakaoAccount(Profile profile, String email) implements KakaoAuthResponse {
        public KakaoAccount(Profile profile, String email) {
            this.profile = profile;
            this.email = email;
        }

        public record Profile(String nickname) {}
    }
}