package com.projectw.domain.waiting.service;

import com.projectw.domain.waiting.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;

}
