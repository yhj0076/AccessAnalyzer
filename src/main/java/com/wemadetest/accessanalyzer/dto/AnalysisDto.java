package com.wemadetest.accessanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class AnalysisDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DetailLogResponse {
        private String path;
        private int httpStatus;
        private String clientIp;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonPropertyOrder({
            "requestCount",
            "successRate",
            "redirectRate",
            "clientErrorRate",
            "serverErrorRate",
            "detailLogs"
    })
    public static class Response{
        private int requestCount;
        private String successRate;
        private String redirectRate;
        private String clientErrorRate;
        private String serverErrorRate;
        private List<DetailLogResponse> detailLogs;
    }
}
