package com.projectw.domain.search;

import com.projectw.common.config.WebSecurityConfig;
import com.projectw.common.enums.UserRole;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc
@MockBean({JpaMetamodelMappingContext.class, JwtUtil.class})
@Import({WebSecurityConfig.class})
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;



    @Test
    void search_성공_응답() throws Exception {
        // given
        KeywordSearchResponse.Search searchResponse = new KeywordSearchResponse.Search("keyword", List.of(), List.of());

        given(searchService.intergratedSearch(any(KeywordSearchRequest.Search.class)))
                .willReturn(searchResponse);
        AuthUser authUser = new AuthUser(1L, "admin@example.com", UserRole.ROLE_ADMIN);

        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);

        // when & then
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"keyword\",\"size\":30}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("keyword"))
                .andExpect(jsonPath("$.data.stores").isArray());
    }

    @Test
    void autoComplete_성공_응답() throws Exception {
        // given
        String keyword = "testKeyword";
        KeywordSearchResponse.AutoComplete autoCompleteResponse = new KeywordSearchResponse.AutoComplete(keyword, List.of());

        given(searchService.autoComplete(eq(keyword)))
                .willReturn(autoCompleteResponse);

        // when & then
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value(keyword))
                .andExpect(jsonPath("$.data.stores").isArray());
    }

}