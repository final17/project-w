package com.projectw.domain.chatbot.service;

public enum ReservationInquiryDetail {
    Reservation_Change("예약 날짜나 시간 변경은 방문 하루 전까지 가능합니다."),
    Cancel_Reservation("예약 취소는 방문 하루 전까지 가능합니다."),
    Reservation_Inquiry("예약 조회는 마이페이지에서 가능합니다.");

    private final String response;

    ReservationInquiryDetail(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public static ReservationInquiryDetail fromDetail(String detail) {
        try {
            return ReservationInquiryDetail.valueOf(detail);
        } catch (IllegalArgumentException e) {
            return null;  // 변환에 실패할 경우 null을 반환
        }
    }
}