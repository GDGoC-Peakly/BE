package com.example.peakly.global.security;


import com.example.peakly.global.apiPayload.code.status.ErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal == null) {
            return null;
        }

        if (principal instanceof Long l) {
            return l;
        }

        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    public static Long requireUserId() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        return userId;
    }
}
