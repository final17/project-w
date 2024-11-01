package com.projectw.domain.chatbot.service;

import com.projectw.domain.chatbot.dto.ChatbotRequestDto;
import com.projectw.domain.chatbot.dto.ChatbotResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    public ChatbotResponseDto processInquiry(ChatbotRequestDto chatbotRequestDto) {
        String responseMessage;

        switch (chatbotRequestDto.getInquiryType()) {
            case "예약문의":
                responseMessage = handleReservationInquiry(chatbotRequestDto.getDetail());
                break;
            case "결제문의":
                responseMessage = handlePaymentInquiry(chatbotRequestDto.getDetail());
                break;
            case "회원문의":
                responseMessage = handleMemberInquiry(chatbotRequestDto.getDetail());
                break;
            case "기타문의":
                responseMessage = handleGeneralInquiry(chatbotRequestDto.getDetail());
                break;
            default:
                responseMessage = "올바른 문의 유형을 선택해주세요.";
        }

        return new ChatbotResponseDto(responseMessage);
    }

    private String handleReservationInquiry(String detail) {
        switch (detail) {
            case "예약변경":
                return "예약 날짜나 시간 변경은 방문 하루 전까지 가능합니다.";
            case "예약취소":
                return "예약 취소는 방문 하루 전까지 가능합니다.";
            case "예약조회":
                return "예약 조회는 마이페이지에서 가능합니다.";
            default:
                return "예약 문의에 대한 추가 정보가 필요합니다.";
        }
    }

    private String handlePaymentInquiry(String detail) {
        switch (detail) {
            case "예약금 환불":
                return "예약금 환불은 예약 취소 즉시 됩니다." +
                        "카드 승인 취소까지 평균 3~5일 정도 소요될 수 있으며, 정확한 환불 일정은 카드사로 문의해주시길 바랍니다.";
            case "결제 카드변경":
                return "예약금 결제 카드 변경은 예약 취소 후 재결제 부탁드립니다.";
            default:
                return "결제 문의에 대한 추가 정보가 필요합니다.";
        }
    }

    private String handleMemberInquiry(String detail) {
        switch (detail) {
            case "회원탈퇴":
                return "회원탈퇴는 마이페이지에서 가능합니다.";
            case "회원가입":
                return "회원 가입은 앱을 통해 손쉽게 하실 수 있습니다.";
            default:
                return "회원 문의에 대한 추가 정보가 필요합니다.";
        }
    }

    private String handleGeneralInquiry(String detail) {
        return "기타 문의에 대해서는 고객센터로 연락 부탁드립니다.";
    }
}