package com.projectw.domain.chatbot;

import com.projectw.domain.chatbot.dto.request.ChatbotRequestDto;
import com.projectw.domain.chatbot.dto.response.ChatbotResponseDto;
import com.projectw.domain.chatbot.service.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotServiceTest {

    @InjectMocks
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processInquiry_ShouldHandleValidInquiry() {
        // given
        ChatbotRequestDto.Basic request = new ChatbotRequestDto.Basic("GENERAL", "Valid inquiry detail");

        // when
        ChatbotResponseDto response = chatbotService.processInquiry(request);

        // then
        assertThat(response).isInstanceOf(ChatbotResponseDto.Basic.class); // 타입 검증
        ChatbotResponseDto.Basic basicResponse = (ChatbotResponseDto.Basic) response;
        assertThat(basicResponse).isNotNull(); // null 체크
        assertThat(basicResponse.responseMessage()).isEqualTo("올바른 문의 유형을 선택해주세요."); // 메시지 확인
    }

    @Test
    void processInquiry_ShouldReturnMessageWhenDetailIsEmpty() {
        // given
        ChatbotRequestDto.Basic request = new ChatbotRequestDto.Basic("GENERAL", "");

        // when
        ChatbotResponseDto response = chatbotService.processInquiry(request);

        // then
        assertThat(response).isInstanceOf(ChatbotResponseDto.Basic.class);
        ChatbotResponseDto.Basic basicResponse = (ChatbotResponseDto.Basic) response;
        assertThat(basicResponse.responseMessage()).isEqualTo("문의 내용을 입력해주세요.");
    }

    @Test
    void processInquiry_ShouldReturnMessageWhenInquiryTypeIsInvalid() {
        // given
        ChatbotRequestDto.Basic request = new ChatbotRequestDto.Basic("INVALID_TYPE", "Some inquiry detail");

        // when
        ChatbotResponseDto response = chatbotService.processInquiry(request);

        // then
        assertThat(response).isInstanceOf(ChatbotResponseDto.Basic.class);
        ChatbotResponseDto.Basic basicResponse = (ChatbotResponseDto.Basic) response;
        assertThat(basicResponse.responseMessage()).isEqualTo("올바른 문의 유형을 선택해주세요.");
    }

    @Test
    void processInquiry_ShouldReturnMessageWhenRequestIsNull() {
        // given
        ChatbotRequestDto.Basic request = null;

        // when
        ChatbotResponseDto response = chatbotService.processInquiry(request);

        // then
        assertThat(response).isInstanceOf(ChatbotResponseDto.Basic.class);
        ChatbotResponseDto.Basic basicResponse = (ChatbotResponseDto.Basic) response;
        assertThat(basicResponse.responseMessage()).isEqualTo("문의 내용을 입력해주세요.");
    }
}