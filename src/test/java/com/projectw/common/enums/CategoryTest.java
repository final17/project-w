package com.projectw.common.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.domain.store.dto.response.CategoryMapperValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    public void 카테고리_경로() {
        String path = Category.JAPANEASE_FOOD.getPath();
        assertThat(path).isEqualTo("음식종류/일식");
    }

    @Test
    public void 카테고리_코드() {
        String code = Category.JAPANEASE_FOOD.getCode();
        assertThat(code).isEqualTo("FT-2");
    }

    @Test
    public void 루트_카테고리_이름() {
        String root = Category.getRootCategory(Category.JAPANEASE_FOOD).getName();
        assertThat(root).isEqualTo(Category.FOOD.getName());
    }

    @Test
    public void 루트_카테고리_코드() {
        String rootCode = Category.getRootCategory(Category.JAPANEASE_FOOD).getCode();
        assertThat(rootCode).isEqualTo(Category.FOOD.getCode());
    }

    @Test
    public void 자식_카테고리_가져오기() {
        List<Category> childCategories = Category.getChildCategories(Category.ROOT);
        childCategories.forEach(System.out::println);
        List<Category> categoriesByDepth = Category.getCategoriesByDepth(1);
        assertThat(childCategories).containsAll(categoriesByDepth);
    }

    @Test
    public void 뎁스_가져오기() throws Exception {
        int jpnDepth = Category.JAPANEASE_FOOD.getDepth();
        int rootDepth = Category.ROOT.getDepth();
        assertThat(jpnDepth).isEqualTo(2);
        assertThat(rootDepth).isEqualTo(0);
    }

    @Test
    public void 카테고리_매퍼() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new CategoryMapperValue(Category.REGION));
        System.out.println(s);
    }
}