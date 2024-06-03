package com.datn.ticket.model.mapper;

import java.util.HashMap;
import java.util.Map;

public class CategoriesMapper {
    private static final Map<String, String> catMap = new HashMap<>();

    static {
        catMap.put("music", "Âm nhạc");
        catMap.put("seminar", "Hội thảo");
        catMap.put("theater", "Sân khấu");
    }

    public static String mapCategory(String frontendCategory) {
        return catMap.get(frontendCategory);
    }
}
