package com.example.peakly.domain.auth.dto.response;

import java.time.OffsetDateTime;

public record EmailVerifySendResponse(
        String email,
        OffsetDateTime sentAt
) {}
