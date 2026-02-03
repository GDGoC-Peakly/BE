package com.example.peakly.domain.auth.dto.response;

import java.time.OffsetDateTime;

public record SignupResponse(
        Long id,
        OffsetDateTime createdAt
) {}
