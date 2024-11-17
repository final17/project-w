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
        if (!(chatbotRequestDto instanceof ChatbotRequestDto.Basic request)) {
            return new ChatbotResponseDto.Basic(EMPTY_DETAIL_MESSAGE);
        }

        if (!isValidRequest(request)) {
            return new ChatbotResponseDto.Basic(EMPTY_DETAIL_MESSAGE);
        }

        InquiryType inquiryType;
        try {
            inquiryType = InquiryType.valueOf(request.inquiryType());
        } catch (IllegalArgumentException e) {
            return new ChatbotResponseDto.Basic(INVALID_INQUIRY_TYPE_MESSAGE);
        }

        String responseMessage = inquiryType.handleInquiry(request.detail());
        return new ChatbotResponseDto.Basic(responseMessage);
    }

    private boolean isValidRequest(ChatbotRequestDto.Basic request) {
        return request != null
                && StringUtils.hasText(request.inquiryType())
                && StringUtils.hasText(request.detail());
    }
}