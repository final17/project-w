package com.projectw.domain.chatbot.service;

import java.util.Optional;

public class ReservationInquiryHandler implements InquiryDetailHandler {
    private static final String INVALID_DETAIL_MESSAGE = "유효하지 않은 예약 문의 세부 사항입니다.";

    @Override
    public String handleDetail(String detail) {
        ReservationInquiryDetail detailEnum = ReservationInquiryDetail.fromDetail(detail);
        return Optional.ofNullable(detailEnum)
                .map(ReservationInquiryDetail::getResponse)
                .orElse(INVALID_DETAIL_MESSAGE);
    }
}