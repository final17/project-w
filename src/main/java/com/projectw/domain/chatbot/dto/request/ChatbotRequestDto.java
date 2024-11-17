package com.projectw.domain.chatbot.dto.request;

public sealed interface ChatbotRequestDto permits ChatbotRequestDto.Basic {

    record Basic(String inquiryType, String detail) implements ChatbotRequestDto { }
}