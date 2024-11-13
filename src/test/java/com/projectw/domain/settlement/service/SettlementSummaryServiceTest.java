package com.projectw.domain.settlement.service;

import com.projectw.domain.settlement.dto.SettlementRequest;
import com.projectw.domain.settlement.dto.SettlementResponse;
import com.projectw.domain.settlement.enums.SummaryType;
import com.projectw.domain.settlement.repository.SettlementSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class SettlementSummaryServiceTest {
    @Mock
    private SettlementSummaryRepository settlementSummaryRepository;

    @InjectMocks
    private SettlementSummaryService settlementSummaryService;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void getSettlementSummary_정상동작() {
        // given
        Long userId = 1L;
        Long storeId = 1L;
        SettlementRequest.Summary request = new SettlementRequest.Summary(SummaryType.DAY , "2024-11-01" , "2024-11-30");
        List<SettlementResponse.Summary> response = List.of(
                new SettlementResponse.Summary("2024-11-01" , 50000L , 5000L , 20L)
        );

        given(settlementSummaryRepository.getSettlementSummary(anyLong() , any())).willReturn(response);

        // when
        response = settlementSummaryService.getSettlementSummary(storeId , request);

        // then
        assertEquals(1 , response.size());
        verify(settlementSummaryRepository).getSettlementSummary(storeId , request);
    }

}