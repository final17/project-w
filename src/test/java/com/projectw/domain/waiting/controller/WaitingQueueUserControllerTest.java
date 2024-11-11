package com.projectw.domain.waiting.controller;

import com.projectw.common.config.WebSecurityConfig;
import com.projectw.common.enums.UserRole;
import com.projectw.domain.waiting.dto.WaitingQueueResponse;
import com.projectw.domain.waiting.service.WaitingHistoryService;
import com.projectw.domain.waiting.service.WaitingQueueService;
import com.projectw.security.AuthUser;
import com.projectw.security.JwtAuthenticationToken;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitingQueueUserController.class)
@AutoConfigureMockMvc
@MockBean({JpaMetamodelMappingContext.class, JwtUtil.class})
@Import({WebSecurityConfig.class})
class WaitingQueueUserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitingQueueService waitingQueueService;

    @MockBean
    private WaitingHistoryService waitingHistoryService;


    @Test
    @WithMockUser(username = "user@com", roles = {"USER"})
    void connectToServer_성공_응답() throws Exception {
        long storeId = 1L;
        SseEmitter sseEmitter = new SseEmitter();
        given(waitingQueueService.connect(any(AuthUser.class), eq(storeId))).willReturn(sseEmitter);

        mockMvc.perform(get("/api/v2/user/stores/{storeId}/waitings/connection", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@com", roles = {"USER"})
    void addToWaitingQueue_성공_응답() throws Exception {
        long storeId = 1L;
        WaitingQueueResponse.Info waitingInfo = new WaitingQueueResponse.Info(1, 101L);

        given(waitingQueueService.addUserToQueue(any(), eq(storeId))).willReturn(waitingInfo);
        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.ROLE_USER);
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);

        mockMvc.perform(post("/api/v2/user/stores/{storeId}/waitings", storeId)
                        .principal(authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rank").value(1))
                .andExpect(jsonPath("$.data.userId").value(101L));
    }

    @Test
    @WithMockUser(username = "user@com", roles = {"USER"})
    void waitingCancel_성공_응답() throws Exception {
        long storeId = 1L;

        doNothing().when(waitingQueueService).cancel(any(), eq(storeId));
        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.ROLE_USER);
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);

        mockMvc.perform(delete("/api/v2/user/stores/{storeId}/waitings", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(username = "user@com", roles = {"USER"})
    void getRegisteredWaitingStoreIds_성공_응답() throws Exception {
        WaitingQueueResponse.MyWaitingStoreList myWaitingStoreList = new WaitingQueueResponse.MyWaitingStoreList(List.of(1L, 2L));

        given(waitingHistoryService.getRegisteredStoreList(any())).willReturn(myWaitingStoreList);

        mockMvc.perform(get("/api/v2/user/waitings/stores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeIds[0]").value(1L))
                .andExpect(jsonPath("$.data.storeIds[1]").value(2L));
    }

    @Test
    @WithMockUser(username = "user@com", roles = {"USER"})
    void checkWaitingStatus_성공_응답() throws Exception {
        long storeId = 1L;
        WaitingQueueResponse.WaitingInfo waitingInfo = new WaitingQueueResponse.WaitingInfo(true);

        given(waitingQueueService.checkWaitingStatus(any(), eq(storeId))).willReturn(waitingInfo);

        mockMvc.perform(get("/api/v2/user/stores/{storeId}/waitings", storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isWaiting").value(true));
    }
}