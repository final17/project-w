package com.projectw.domain.chatbot.service;

import java.util.Optional;

public class MemberInquiryHandler implements InquiryDetailHandler {
    private static final String INVALID_DETAIL_MESSAGE = "유효하지 않은 회원 문의 세부 사항입니다.";

    @Override
    public String handleDetail(String detail) {
        MemberInquiryDetail detailEnum = MemberInquiryDetail.fromDetail(detail);
        return Optional.ofNullable(detailEnum)
                .map(MemberInquiryDetail::getResponse)
                .orElse(INVALID_DETAIL_MESSAGE);
    }
}