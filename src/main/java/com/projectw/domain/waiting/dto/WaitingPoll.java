package com.projectw.domain.waiting.dto;

import java.time.LocalDateTime;

public record WaitingPoll(Long waitingNum, Long storeId, Long userId, LocalDateTime createdAt) { }
