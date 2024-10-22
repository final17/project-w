package com.projectw.domain.confirmation.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConfirmationEvent {
    private final Long targetId;
}
