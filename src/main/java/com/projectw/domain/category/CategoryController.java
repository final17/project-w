package com.projectw.domain.category;

import com.projectw.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/categories")
    public ResponseEntity<SuccessResponse<List<CategoryMapperValue>>> categoryController(@RequestParam(name = "type") CategoryType type) {

        return ResponseEntity.ok(SuccessResponse.of(categoryService.getCategories(type)));
    }
}
