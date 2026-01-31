package com.example.peakly.domain.auth.dto.response;

public record CheckEmailResponse(
        String email,
        boolean isDuplicated
) {}
