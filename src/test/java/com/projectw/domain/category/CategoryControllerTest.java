package com.projectw.domain.category;

import com.projectw.common.config.WebSecurityConfig;
import com.projectw.domain.search.SearchService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc
@MockBean({JpaMetamodelMappingContext.class, JwtUtil.class})
@Import({WebSecurityConfig.class})
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getCategories_성공_응답() throws Exception {
        // given
        CategoryType type = CategoryType.DISTRICT;
        List<CategoryMapperValue> mockResponse = Arrays.stream(DistrictCategory.values()).map(CategoryMapperValue::new).toList();
        DistrictCategory[] values = DistrictCategory.values();
        given(categoryService.getCategories(type)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/categories")
                        .param("type", type.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value(values[0].getName()))
                .andExpect(jsonPath("$.data[1].name").value(values[1].getName()));
    }
}