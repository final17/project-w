package com.projectw.domain.waiting.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.domain.waiting.dto.WaitingPoll;
import com.projectw.domain.waiting.dto.WaitingPollEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Transactional
public class WaitingPollEventListener {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @EventListener
    public void onWaitingPollEvent(WaitingPollEvent waitingPollEvent) {
        WaitingPoll info = (WaitingPoll) waitingPollEvent.getSource();
        LocalDateTime at = info.createdAt();
        Long userId = info.userId();
        Long storeId = info.storeId();
        Long num = info.waitingNum();

        User user = userRepository.findById(userId).orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        Store store = storeRepository.findById(storeId).orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.APPLY)
                .type(ReservationType.WAIT)
                .user(user)
                .store(store)
                .reservationDate(at.toLocalDate())
                .reservationTime(at.toLocalTime().truncatedTo(TimeUnit.SECONDS.toChronoUnit()))
                .numberPeople(1L)
                .reservationNo(num)
                .menuYN(false)
                .build();

        reservationRepository.save(reservation);
    }
}
