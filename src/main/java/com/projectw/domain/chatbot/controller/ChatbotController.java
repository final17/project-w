package com.projectw.domain.chatbot.controller;

import com.projectw.domain.chatbot.dto.request.ChatbotRequestDto;
import com.projectw.domain.chatbot.dto.response.ChatbotResponseDto;
import com.projectw.domain.chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/inquiry")
    public ResponseEntity<ChatbotResponseDto.Basic> handleInquiry(@RequestBody ChatbotRequestDto.Basic chatbotRequestDto) {
        ChatbotResponseDto.Basic response = (ChatbotResponseDto.Basic) chatbotService.processInquiry(chatbotRequestDto);
        return ResponseEntity.ok(response);
    }
}