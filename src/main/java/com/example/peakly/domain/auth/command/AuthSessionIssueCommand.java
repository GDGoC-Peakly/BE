package com.example.peakly.domain.auth.command;

import java.time.LocalDateTime;

public record AuthSessionIssueCommand(
        String deviceId,
        String refreshTokenHash,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt
) {
}
