package com.example.peakly.domain.report.enums;

public enum Insight {
    BEST_CONDITION("최상의 리듬입니다! 🌊"),
    GOOD("좋은 흐름이에요! 👍"),
    TIRED("조금 지치셨네요 💦"),
    FOCUS_NEEDED("집중이 필요해요 🔥"),
    BAD("조금 더 힘내봐요! 🔥");

    private final String message;

    Insight(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static Insight from(double achievementRate, double accuracyRate) {
        if (achievementRate >= 80 && accuracyRate >= 80) return BEST_CONDITION;
        if (achievementRate >= 60 && accuracyRate >= 60) return GOOD;
        if (achievementRate >= 40 || accuracyRate >= 40) return TIRED;
        if (achievementRate >= 20 || accuracyRate >= 20) return FOCUS_NEEDED;
        return BAD;
    }
}
