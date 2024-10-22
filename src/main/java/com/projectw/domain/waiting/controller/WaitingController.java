package com.projectw.domain.waiting.controller;

import com.projectw.domain.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/waiting")
public class WaitingController {

    private final WaitingService waitingService;

}
