package com.projectw.domain.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    public List<CategoryMapperValue> getCategories(CategoryType type) {
        HierarchicalCategory root = switch (type) {
            case FOOD_KIND-> FoodKindCategory.ROOT;
            case CUISINE -> CuisineCategory.ROOT;
            case DISTRICT -> DistrictCategory.ROOT;
            default -> null;
        };

        return HierarchicalCategoryUtils
                .getChildCategories(root)
                .stream()
                .map(CategoryMapperValue::new)
                .toList();
    }

    @Test
    public void 음식종류_카테고리_목록_조회() throws Exception {
        // given
        List<String> list = Arrays.stream(FoodKindCategory.values()).map(FoodKindCategory::getName).toList();
        // when
        List<String> res = categoryService.getCategories(CategoryType.FOOD_KIND).stream().map(CategoryMapperValue::getName)
                .toList();
        // then
        assertThat(res).containsAll(list);
    }

    @Test
    public void 지역_카테고리_목록_조회() throws Exception {
        // given
        List<String> list = Arrays.stream(DistrictCategory.values()).map(DistrictCategory::getName).toList();
        // when
        List<String> res = categoryService.getCategories(CategoryType.DISTRICT).stream().map(CategoryMapperValue::getName)
                .toList();
        // then
        assertThat(res).containsAll(list);
    }

    @Test
    public void CUISINE_카테고리_목록_조회() throws Exception {
        // given
        List<String> list = Arrays.stream(CuisineCategory.values()).map(CuisineCategory::getName).toList();
        // when
        List<String> res = categoryService.getCategories(CategoryType.CUISINE).stream().map(CategoryMapperValue::getName)
                .toList();
        // then
        assertThat(res).containsAll(list);
    }
}