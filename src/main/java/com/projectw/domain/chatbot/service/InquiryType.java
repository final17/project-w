package com.projectw.domain.chatbot.service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
enum InquiryType {
    RESERVATION(new ReservationInquiryHandler()),
    PAYMENT(new PaymentInquiryHandler()),
    MEMBERSHIP(new MemberInquiryHandler()),
    OTHER(detail -> "기타 문의에 대해서는 고객센터로 연락 부탁드립니다.");

    private final InquiryDetailHandler handler;

    public String handleInquiry(String detail) {
        return handler.handleDetail(detail);
    }
}