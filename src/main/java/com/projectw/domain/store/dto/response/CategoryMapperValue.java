package com.projectw.domain.store.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryMapperValue {
    private String type;
    private String name;
    private String code;
    private String path;
    private int depth;

    public CategoryMapperValue(CategoryMapper mapper) {
        this.type =  mapper.getType();
        this.code = mapper.getCode();
        this.name = mapper.getName();
        this.path = mapper.getPath();
        this.depth = mapper.getDepth();
    }
}
