package com.erick.order_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;

    private boolean hasNext;

    private boolean hasPrevious;

    public static <T> PageResponse<T> from(
            Page<T> springPage
    ) {
        return PageResponse
                .<T>builder()
                .content(
                        springPage.getContent()
                )
                .page(
                        springPage.getNumber()
                )
                .size(
                        springPage.getSize()
                )
                .totalElements(
                        springPage.getTotalElements()
                )
                .totalPages(
                        springPage.getTotalPages()
                )
                .first(
                        springPage.isFirst()
                )
                .last(
                        springPage.isLast()
                )
                .hasNext(
                        springPage.hasNext()
                )
                .hasPrevious(
                        springPage.hasPrevious()
                )
                .build();
    }
}

