package com.BEProjects.url_shortener.domain.models;

import org.springframework.data.domain.Page;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public record PagedResult<T>(
        List<T> data,
        int pageNumber,
        int totalPages,
        long totalElements,
        boolean isFirst,
        boolean isLast,
        boolean hasNext,
        boolean hasPrevious) {

    public static <T> PagedResult<T> from(Page<T> page) {
        return new PagedResult<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
