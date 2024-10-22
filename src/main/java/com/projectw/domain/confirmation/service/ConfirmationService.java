package com.projectw.domain.confirmation.service;

import com.projectw.domain.confirmation.repository.ConfirmationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;

    public void save() {

    }
}
