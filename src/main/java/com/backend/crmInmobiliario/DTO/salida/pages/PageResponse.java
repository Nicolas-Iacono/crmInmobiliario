package com.backend.crmInmobiliario.DTO.salida.pages;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int totalPages,
        long totalElements
) {}
