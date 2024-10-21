package com.projectw.domain.reservation.service;

import com.projectw.common.enums.ReservationStatus;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.security.AuthUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    public void 예약_생성() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.ROLE_USER);
        User user = User.fromAuthUser(authUser);
        LocalDate localDate = LocalDate.of(2024, 10, 29);
        LocalTime localTime = LocalTime.of(15, 15, 0);
        ReserveRequest.Create request = new ReserveRequest.Create(1L, 1L, 1, localDate, localTime);
        Reservation reservation = new Reservation(user, request.reservationNumber(), request.numberOfGuests(), request.reservationDate(), request.reservationTime());
        ReflectionTestUtils.setField(reservation, "id", 1L);
        given(reservationRepository.save(any())).willReturn(reservation);

        // when
        ReserveResponse.Info data = reservationService.reserve(authUser, 1L, request).getData();

        // then
        assertThat(data.numberOfGuests()).isEqualTo(1);
        assertThat(data.reservationDate()).isEqualTo(localDate);
        assertThat(data.reservationTime()).isEqualTo(localTime);
        assertThat(data.userId()).isEqualTo(1L);
        assertThat(data.reserveId()).isEqualTo(1L);
        assertThat(data.status()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    public void 예약_생성_스토어를_찾을_수_없다면_예외발생() throws Exception {
        // given
//        AuthUser authUser = new AuthUser(1L, "email", UserRole.ROLE_USER);
//        User user = User.fromAuthUser(authUser);
//        LocalDate localDate = LocalDate.of(2024, 10, 29);
//        LocalTime localTime = LocalTime.of(15, 15, 0);
//        ReserveRequest.Create request = new ReserveRequest.Create(1L, 1L, 1, localDate, localTime);
//        Reservation reservation = new Reservation(user, request.reservationNumber(), request.numberOfGuests(), request.reservationDate(), request.reservationTime());
//        ReflectionTestUtils.setField(reservation, "id", 1L);

        // given(storeRepository.findById(any())).willReturn(Optional.empty());

//        given(reservationRepository.save(any())).willReturn(reservation);

        // when then
//        assertThatThrownBy(()-> reservationService.reserve(authUser, 1L, request))
//                .isInstanceOf(InvalidRequestException.class)
//                .hasMessage("");
    }
}