package com.projectw.domain.waiting.controller;

import com.projectw.common.config.WebSecurityConfig;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.service.WaitingQueueService;
import com.projectw.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitingOwnerController.class)
@AutoConfigureMockMvc
@MockBean({JpaMetamodelMappingContext.class, JwtUtil.class})
@Import({WebSecurityConfig.class})
class WaitingOwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitingQueueService storeQueueService;

    @Test
    @WithMockUser(username = "owner@com", roles = {"OWNER"})
    void getWaitingList_성공_응답() throws Exception {
        // given
        long storeId = 1L;
        WaitingQueueResponse.List waitingList = new WaitingQueueResponse.List(2, List.of(
                new WaitingQueueResponse.Info(1, 101L),
                new WaitingQueueResponse.Info(2, 102L)
        ));

        given(storeQueueService.getWaitingList(any(), eq(storeId))).willReturn(waitingList);

        // when  then
        mockMvc.perform(get("/api/v2/owner/stores/{storeId}/waitings/list", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalWaitingNumber").value(2))
                .andExpect(jsonPath("$.data.userIds[0].rank").value(1))
                .andExpect(jsonPath("$.data.userIds[0].userId").value(101L))
                .andExpect(jsonPath("$.data.userIds[1].rank").value(2))
                .andExpect(jsonPath("$.data.userIds[1].userId").value(102L));
    }

    @Test
    @WithMockUser(username = "owner@com", roles = {"OWNER"})
    void pollUser_성공_응답() throws Exception {
        // given
        long storeId = 1L;
        doNothing().when(storeQueueService).pollFirstUser(any(), eq(storeId));

        // when & then
        mockMvc.perform(post("/api/v2/owner/stores/{storeId}/waitings/poll", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(username = "owner@com", roles = {"OWNER"})
    void waitingCut_성공_응답() throws Exception {
        // given
        long storeId = 1L;
        int cutline = 3;
        doNothing().when(storeQueueService).clearQueueFromRank(any(), eq(storeId), eq(cutline));
        // when & then
        mockMvc.perform(delete("/api/v2/owner/stores/{storeId}/waitings/clear", storeId)
                        .param("cutline", String.valueOf(cutline))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}