package com.projectw.domain.chatbot.service;

import com.projectw.domain.chatbot.dto.request.ChatbotRequestDto;
import com.projectw.domain.chatbot.dto.response.ChatbotResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private static final String INVALID_INQUIRY_TYPE_MESSAGE = "올바른 문의 유형을 선택해주세요.";
    private static final String EMPTY_DETAIL_MESSAGE = "문의 내용을 입력해주세요.";

    public ChatbotResponseDto processInquiry(ChatbotRequestDto chatbotRequestDto) {
        if (!isValidRequest(chatbotRequestDto)) {
            return new ChatbotResponseDto(EMPTY_DETAIL_MESSAGE);
        }

        InquiryType inquiryType;
        try {
            inquiryType = InquiryType.valueOf(chatbotRequestDto.getInquiryType());
        } catch (IllegalArgumentException e) {
            return new ChatbotResponseDto(INVALID_INQUIRY_TYPE_MESSAGE);
        }

        String responseMessage = inquiryType.handleInquiry(chatbotRequestDto.getDetail());
        return new ChatbotResponseDto(responseMessage);
    }

    // 요청 유효성 검사 메서드
    private boolean isValidRequest(ChatbotRequestDto chatbotRequestDto) {
        return chatbotRequestDto != null
                && StringUtils.hasText(chatbotRequestDto.getInquiryType())
                && StringUtils.hasText(chatbotRequestDto.getDetail());
    }
}