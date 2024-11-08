package com.projectw.domain.chatbot.service;

public enum PaymentInquiryDetail {
    Deposit_Refund("예약금 환불 시 카드 승인 취소까지 평균 3~5일 정도 소요될 수 있으며, 정확한 환불 일정은 카드사로 문의해주시길 바랍니다."),
    Payment_Card_Change("예약금 결제 카드 변경은 예약 취소 후 재결제 부탁드립니다.");

    private final String response;

    PaymentInquiryDetail(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public static PaymentInquiryDetail fromDetail(String detail) {
        try {
            return PaymentInquiryDetail.valueOf(detail);
        } catch (IllegalArgumentException e) {
            return null;  // 변환에 실패할 경우 null을 반환
        }
    }
}