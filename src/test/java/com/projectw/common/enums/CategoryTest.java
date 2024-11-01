package com.projectw.common.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectw.domain.category.FoodCategory;
import com.projectw.domain.category.HierarchicalCategory;
import com.projectw.domain.category.HierarchicalCategoryUtils;
import com.projectw.domain.category.RegionCategory;
import com.projectw.domain.store.dto.response.CategoryMapperValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    public void 카테고리_경로() {
        String path = FoodCategory.JAPANESE_FOOD.getPath();
        assertThat(path).isEqualTo("음식종류/일식");
    }

    @Test
    public void 카테고리_코드() {
        String code = FoodCategory.JAPANESE_FOOD.getCode();
        assertThat(code).isEqualTo("FT-2");
    }

    @Test
    public void 루트_카테고리_이름() {
        String name = FoodCategory.ROOT.getName();
        assertThat(name).isEqualTo("음식종류");
    }

    @Test
    public void 루트_카테고리_코드() {
        String code = FoodCategory.ROOT.getCode();
        assertThat(code).isEqualTo("FT");
    }

    @Test
    public void 자식_카테고리_가져오기() {

        List<HierarchicalCategory> childCategories = HierarchicalCategoryUtils.getChildCategories(FoodCategory.JAPANESE_FOOD);
        childCategories.forEach(System.out::println);

        List<HierarchicalCategory> categoriesByDepth = HierarchicalCategoryUtils.getCategoriesByDepth(FoodCategory.class, 2);
        assertThat(childCategories).containsAll(categoriesByDepth);
    }

    @Test
    public void 뎁스_가져오기() throws Exception {
        int jpnDepth = FoodCategory.JAPANESE_FOOD.getDepth();
        int rootDepth = FoodCategory.ROOT.getDepth();
        assertThat(jpnDepth).isEqualTo(1);
        assertThat(rootDepth).isEqualTo(0);
    }

    @Test
    public void 카테고리_매퍼() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new CategoryMapperValue(FoodCategory.KOREAN_FOOD));
        System.out.println(s);
    }

    @Test
    public void t() throws Exception {
        List<HierarchicalCategory> categoriesByDepth = HierarchicalCategoryUtils.getCategoriesByDepth(FoodCategory.class, 2);
        categoriesByDepth.forEach(System.out::println);

        List<HierarchicalCategory> categoriesByDepth1 = HierarchicalCategoryUtils.getCategoriesByDepth(RegionCategory.class, 1);
        categoriesByDepth1.forEach(System.out::println);

        HierarchicalCategory rootCategory = HierarchicalCategoryUtils.getRootCategory(RegionCategory.class);
        String name = rootCategory.getName();
        System.out.println("name = " + name);

        String name1 = FoodCategory.JAPANESE_FOOD.getRoot().getName();
        System.out.println("name1 = " + name1);

        List<String> childrenPaths = HierarchicalCategoryUtils.getChildrenPaths(RegionCategory.SEOUL);
        List<String> childrenCodes = HierarchicalCategoryUtils.getChildrenCodes(RegionCategory.SEOUL);
        List<String> childrenNames = HierarchicalCategoryUtils.getChildrenNames(RegionCategory.SEOUL);
        childrenPaths.forEach(System.out::println);
        childrenCodes.forEach(System.out::println);
        childrenNames.forEach(System.out::println);
    }
}