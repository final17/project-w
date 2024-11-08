package com.projectw.domain.chatbot.service;

public enum MemberInquiryDetail {
    Withdrawal("회원탈퇴는 마이페이지에서 가능합니다."),
    Signup("회원가입은 앱을 통해 손쉽게 하실 수 있습니다.");

    private final String response;

    MemberInquiryDetail(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public static MemberInquiryDetail fromDetail(String detail) {
        try {
            return MemberInquiryDetail.valueOf(detail);
        } catch (IllegalArgumentException e) {
            return null;  // 변환에 실패할 경우 null을 반환
        }
    }
}