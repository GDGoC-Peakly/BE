package com.example.peakly.domain.auth.dto.response;

import java.time.LocalDateTime;

public record SignupResponse(
        Long id,
        LocalDateTime createdAt
) {}
