package com.projectw.domain.settlement.service;

import com.projectw.domain.payment.enums.PaymentMethod;
import com.projectw.domain.payment.enums.Status;
import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import com.projectw.domain.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {
    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private SettlementService settlementService;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void getSettlementLog_정상동작() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        SettlementRequest.Log request = new SettlementRequest.Log(LocalDate.parse("2024-11-01") , LocalDate.parse("2024-11-30") , 1 , 10);
        Pageable pageable = PageRequest.of(request.page() - 1, request.size());
        List<SettlementResponse.Log> response = List.of(
                new SettlementResponse.Log(1L , "ORDER-ASDHASDAS231" , PaymentMethod.CARD , 5000L , OffsetDateTime.now() , LocalDate.parse("2024-11-01") , LocalDate.parse("2024-11-01") , Status.COMPLETED)
        );

        Page<SettlementResponse.Log> page = new PageImpl<>(response , pageable , 1L);
        given(settlementRepository.getSettlementLog(anyLong() , anyLong() , any())).willReturn(page);

        // when
        page = settlementService.getSettlementLog(storeId , userId , request);

        // then
        assertEquals(response.get(0).orderId() , page.getContent().get(0).orderId());
    }
}