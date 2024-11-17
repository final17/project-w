package com.projectw.domain.category;

import java.util.ArrayList;
import java.util.List;

public enum CuisineCategory implements HierarchicalCategory {

    ROOT("cuisine","CUISINE", "C", null),
        KOREAN_FOOD("cuisine","한식", "1", ROOT),
        JAPANESE_FOOD("cuisine","일식", "2", ROOT);


    private final String type;
    private final String name;
    private final String code;
    private final HierarchicalCategory parent;
    private final List<HierarchicalCategory> children;
    private final int depth;

    CuisineCategory(String type, String name, String code, HierarchicalCategory parent) {
        this.type = type;
        this.name = name;
        this.parent = parent;
        this.code = code;
        children = new ArrayList<>();

        // root
        if(parent == null){
            depth = 0;
        } else {
            depth = 1 + parent.getDepth();
            parent.getChildren().add(this);
        }
    }

    @Override
    public String getCode() {
        if(parent == null) return code;

        return parent.getCode() + code;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        if(parent == ROOT || parent == null) return name;

        return parent.getPath() + "/" + name;
    }

    @Override
    public int getDepth() {
        return depth;
    }


    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public boolean isRoot() {
        return this == ROOT;
    }

    @Override
    public List<HierarchicalCategory> getChildren() {
        return children;
    }

    @Override
    public HierarchicalCategory getParent() {
        return parent;
    }

    @Override
    public HierarchicalCategory getRoot() {
        HierarchicalCategory t = this;

        while(!t.isRoot()) {
            t = t.getParent();
        }

        return t;
    }
}
