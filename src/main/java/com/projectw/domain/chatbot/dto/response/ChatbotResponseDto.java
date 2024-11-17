package com.projectw.domain.chatbot.dto.response;

public sealed interface ChatbotResponseDto permits ChatbotResponseDto.Basic {

    record Basic(String responseMessage) implements ChatbotResponseDto { }
}