package com.example.peakly.domain.peakTimePrediction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record PeakWindowJson(
        Integer hour, //시작시간
        Double score  //예상 집중도
        ) {
}
