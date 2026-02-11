package com.example.peakly.domain.report.enums;

public enum Insight {
    BEST_CONDITION("최상의 리듬입니다! 🌊"),
    TIRED("조금 지치셨네요 💦"),
    FOCUS_NEEDED("집중이 필요해요 🔥");

    private final String message;

    Insight(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
