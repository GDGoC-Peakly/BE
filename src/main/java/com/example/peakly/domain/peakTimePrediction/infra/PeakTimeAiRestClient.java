package com.example.peakly.domain.peakTimePrediction.infra;

import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictRequest;
import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictResponse;
import com.example.peakly.global.apiPayload.code.status.PeakTimePredictionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class PeakTimeAiRestClient implements PeakTimeAiClient {

    private final RestClient restClient;

    public PeakTimeAiRestClient(
            @Qualifier("peakTimeAiHttpClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    @Override
    public PeakTimePredictResponse predict(PeakTimePredictRequest req) {
        try {
            return restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(PeakTimePredictResponse.class);
        } catch (HttpStatusCodeException e) {
            log.error("AI 응답 오류 status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new GeneralException(PeakTimePredictionErrorStatus.AI_BAD_RESPONSE);
        } catch (RestClientException e) {
            log.error("AI 통신 실패", e);
            throw new GeneralException(PeakTimePredictionErrorStatus.AI_SERVER_ERROR);
        }
    }
}