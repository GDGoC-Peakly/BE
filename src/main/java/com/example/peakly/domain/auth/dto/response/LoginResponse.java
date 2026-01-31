package com.example.peakly.domain.auth.dto.response;

import com.example.peakly.domain.user.entity.Job;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserSummary user
) {
    public record UserSummary(Long id, String email, Job job) {}
}
