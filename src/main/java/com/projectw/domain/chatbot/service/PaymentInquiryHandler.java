package com.projectw.domain.chatbot.service;

import java.util.Optional;

public class PaymentInquiryHandler implements InquiryDetailHandler {
    private static final String INVALID_DETAIL_MESSAGE = "유효하지 않은 결제 문의 세부 사항입니다.";

    @Override
    public String handleDetail(String detail) {
        PaymentInquiryDetail detailEnum = PaymentInquiryDetail.fromDetail(detail);
        return Optional.ofNullable(detailEnum)
                .map(PaymentInquiryDetail::getResponse)
                .orElse(INVALID_DETAIL_MESSAGE);
    }
}