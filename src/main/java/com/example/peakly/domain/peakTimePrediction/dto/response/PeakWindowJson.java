package com.example.peakly.domain.peakTimePrediction.dto.response;


public record PeakWindowJson(
        Double hour, //시작시간
        Double duration,
        Double score  //예상 집중도
) {}
