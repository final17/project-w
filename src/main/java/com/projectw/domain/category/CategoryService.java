package com.projectw.domain.category;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    public List<CategoryMapperValue> getCategories(CategoryType type) {
        HierarchicalCategory root = switch (type) {
            case FOOD_KIND-> FoodKindCategory.ROOT;
            case CUISINE -> CuisineCategory.ROOT;
            case DISTRICT -> DistrictCategory.ROOT;
            default -> null;
        };

        if(root == null) {
            return List.of();
        }

        return HierarchicalCategoryUtils
                .getChildCategories(root)
                .stream()
                .map(CategoryMapperValue::new)
                .toList();
    }
}
