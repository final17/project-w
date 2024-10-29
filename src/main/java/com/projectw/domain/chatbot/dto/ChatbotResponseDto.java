package com.projectw.domain.chatbot.dto;

import lombok.Getter;

@Getter
public class ChatbotResponseDto {
    private String responseMessage;

    public ChatbotResponseDto(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}