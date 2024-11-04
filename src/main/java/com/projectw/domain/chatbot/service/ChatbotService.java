package com.projectw.domain.chatbot.service;

import com.projectw.domain.chatbot.dto.ChatbotRequestDto;
import com.projectw.domain.chatbot.dto.ChatbotResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    public ChatbotResponseDto processInquiry(ChatbotRequestDto chatbotRequestDto) {
        InquiryType inquiryType;
        try {
            inquiryType = InquiryType.valueOf(chatbotRequestDto.getInquiryType());
        } catch (IllegalArgumentException e) {
            return new ChatbotResponseDto("올바른 문의 유형을 선택해주세요.");
        }

        String responseMessage = inquiryType.handleInquiry(chatbotRequestDto.getDetail());
        return new ChatbotResponseDto(responseMessage);
    }
}

enum InquiryType {
    예약문의 {
        @Override
        public String handleInquiry(String detail) {
            return ReservationInquiryDetail.fromDetail(detail).getResponse();
        }
    },
    결제문의 {
        @Override
        public String handleInquiry(String detail) {
            return PaymentInquiryDetail.fromDetail(detail).getResponse();
        }
    },
    회원문의 {
        @Override
        public String handleInquiry(String detail) {
            return MemberInquiryDetail.fromDetail(detail).getResponse();
        }
    },
    기타문의 {
        @Override
        public String handleInquiry(String detail) {
            return "기타 문의에 대해서는 고객센터로 연락 부탁드립니다.";
        }
    };

    public abstract String handleInquiry(String detail);
}

enum ReservationInquiryDetail {
    예약변경("예약 날짜나 시간 변경은 방문 하루 전까지 가능합니다."),
    예약취소("예약 취소는 방문 하루 전까지 가능합니다."),
    예약조회("예약 조회는 마이페이지에서 가능합니다.");

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
            return null;
        }
    }
}

enum PaymentInquiryDetail {
    예약금환불("예약금 환불 시 카드 승인 취소까지 평균 3~5일 정도 소요될 수 있으며, 정확한 환불 일정은 카드사로 문의해주시길 바랍니다."),
    결제카드변경("예약금 결제 카드 변경은 예약 취소 후 재결제 부탁드립니다.");

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
            return null;
        }
    }
}

enum MemberInquiryDetail {
    회원탈퇴("회원탈퇴는 마이페이지에서 가능합니다."),
    회원가입("회원가입은 앱을 통해 손쉽게 하실 수 있습니다.");

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
            return null;
        }
    }
}