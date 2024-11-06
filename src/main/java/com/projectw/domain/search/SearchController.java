package com.projectw.domain.search;

import com.projectw.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public String test() {
        return "paytest";
    }

    @ResponseBody
    @PostMapping("/api/search")
    public ResponseEntity<SuccessResponse<KeywordSearchResponse.Search>> search(
            @RequestBody KeywordSearchRequest.Search search) {
       return ResponseEntity.ok(SuccessResponse.of(searchService.intergratedSearch(search)));
    }

    @ResponseBody
    @GetMapping("/api/search/autocomplete")
    public ResponseEntity<SuccessResponse<KeywordSearchResponse.AutoComplete>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(SuccessResponse.of(searchService.autoComplete(keyword)));
    }
}
