package com.projectw.common.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.domain.category.CuisineCategory;
import com.projectw.domain.category.HierarchicalCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import com.projectw.domain.store.dto.response.CategoryMapperValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    public void 카테고리_경로() {
        String path = CuisineCategory.JAPANESE_FOOD.getPath();
        assertThat(path).isEqualTo("일식");
    }

    @Test
    public void 카테고리_코드() {
        String code = CuisineCategory.JAPANESE_FOOD.getCode();
        assertThat(code).isEqualTo("C2");
    }

    @Test
    public void 루트_카테고리_이름() {
        String name = CuisineCategory.ROOT.getName();
        assertThat(name).isEqualTo("CUISINE");
    }

    @Test
    public void 루트_카테고리_코드() {
        String code = CuisineCategory.ROOT.getCode();
        assertThat(code).isEqualTo("C");
    }

    @Test
    public void 자식_카테고리_가져오기() {

        List<HierarchicalCategory> childCategories = HierarchicalCategoryUtils.getChildCategories(CuisineCategory.JAPANESE_FOOD);
        childCategories.forEach(System.out::println);

        List<HierarchicalCategory> categoriesByDepth = HierarchicalCategoryUtils.getCategoriesByDepth(CuisineCategory.class, 2);
        assertThat(childCategories).containsAll(categoriesByDepth);
    }

    @Test
    public void 뎁스_가져오기() throws Exception {
        int jpnDepth = CuisineCategory.JAPANESE_FOOD.getDepth();
        int rootDepth = CuisineCategory.ROOT.getDepth();
        assertThat(jpnDepth).isEqualTo(1);
        assertThat(rootDepth).isEqualTo(0);
    }

    @Test
    public void 카테고리_매퍼() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new CategoryMapperValue(CuisineCategory.KOREAN_FOOD));
        System.out.println(s);
    }
}