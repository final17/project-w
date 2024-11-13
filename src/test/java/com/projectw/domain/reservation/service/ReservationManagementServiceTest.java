package com.projectw.domain.reservation.service;

import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.domain.payment.event.PaymentCancelEvent;
import com.projectw.domain.reservation.component.ReservationCheckService;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReservationManagementServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationCheckService reservationCheckService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationManagementService reservationManagementService;

    private final String PREFIX_ORDER_ID = "ORDER-";

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void refusalReservation_정상동작() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        String cancelReason = "단순변심";
        ReserveRequest.Cancel cancel = new ReserveRequest.Cancel(cancelReason);

        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.APPLY)
                .paymentYN(true)
                .build();

        given(reservationRepository.findReservationById(anyLong())).willReturn(Optional.of(reservation));

        doNothing().when(reservationCheckService).isOwnerReservation(userId , reservation);
        doNothing().when(reservationCheckService).canChangeReservationStatus(reservation , ReservationStatus.RESERVATION , ResponseCode.REFUSAL_FORBIDDEN);

        doNothing().when(eventPublisher).publishEvent(any(PaymentCancelEvent.class));

        // when
        reservationManagementService.refusalReservation(userId , reservationId , cancel);

        // then
        assertEquals(ReservationStatus.CANCEL , reservation.getStatus());
        verify(eventPublisher).publishEvent(any(PaymentCancelEvent.class));
    }

    @Test
    public void refusalReservation_결제정보없음_예외처리() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        String cancelReason = "단순변심";
        ReserveRequest.Cancel cancel = new ReserveRequest.Cancel(cancelReason);

        given(reservationRepository.findReservationById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> reservationManagementService.refusalReservation(userId , reservationId , cancel));

        // then
        assertEquals(ResponseCode.NOT_FOUND_RESERVATION.getMessage() , exception.getMessage());
    }

    @Test
    public void applyReservation_정상동작() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.RESERVATION)
                .build();
        given(reservationRepository.findReservationById(anyLong())).willReturn(Optional.of(reservation));

        doNothing().when(reservationCheckService).isOwnerReservation(userId , reservation);
        doNothing().when(reservationCheckService).canChangeReservationStatus(reservation , ReservationStatus.RESERVATION , ResponseCode.APPLY_FORBIDDEN);

        // when
        reservationManagementService.applyReservation(userId , reservationId);

        // then
        assertEquals(ReservationStatus.APPLY , reservation.getStatus());
    }

    @Test
    public void applyReservation_결제정보_없음() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        given(reservationRepository.findReservationById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> reservationManagementService.applyReservation(userId , reservationId));

        // then
        assertEquals(ResponseCode.NOT_FOUND_RESERVATION.getMessage() , exception.getMessage());
    }

    @Test
    public void completeReservation_정상동작() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.APPLY)
                .build();
        given(reservationRepository.findReservationById(anyLong())).willReturn(Optional.of(reservation));

        doNothing().when(reservationCheckService).isOwnerReservation(userId , reservation);
        doNothing().when(reservationCheckService).canChangeReservationStatus(reservation , ReservationStatus.APPLY , ResponseCode.COMPLETE_FORBIDDEN);

        // when
        reservationManagementService.completeReservation(userId , reservationId);

        // then
        assertEquals(ReservationStatus.COMPLETE , reservation.getStatus());
    }

    @Test
    public void completeReservation_결제정보_없음() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        given(reservationRepository.findReservationById(anyLong())).willReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class, () -> reservationManagementService.completeReservation(userId , reservationId));

        // then
        assertEquals(ResponseCode.NOT_FOUND_RESERVATION.getMessage() , exception.getMessage());
    }

    @Test
    public void getOnwerReservations_정상동작() {
        // given
        Long userId = 1L;
        String orderId = PREFIX_ORDER_ID + "ASDAS232ASDAS";
        ReserveRequest.Parameter parameter = new ReserveRequest.Parameter(ReservationType.RESERVATION , ReservationStatus.RESERVATION , LocalDate.parse("2024-11-10") , LocalDate.parse("2024-11-20") , 1 , 10);

        List<ReserveResponse.Infos> infos = List.of(
                new ReserveResponse.Infos(orderId , 1L , 1L , 1L , 1L , 1L , LocalDate.parse("2024-11-12") , LocalTime.parse("11:00:00") , ReservationType.RESERVATION , ReservationStatus.RESERVATION)
        );
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        Page<ReserveResponse.Infos> page = new PageImpl<>(infos , pageable , 1L);

        given(reservationRepository.getOwnerReservations(anyLong(), any() , any())).willReturn(page);

        // when
        page = reservationManagementService.getOnwerReservations(userId , parameter);

        // then
        assertEquals(orderId , page.getContent().get(0).orderId());
    }
}
