package com.wemadetest.accessanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class AnalysisDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonPropertyOrder({
            "clientIp",
            "httpStatus",
            "path",
            "country",
            "asn",
            "as_name",
            "as_domain"
    })
    public static class DetailLogResponse {
        private String path;
        private int httpStatus;
        private String clientIp;
        private String country;
        private String asn;
        private String as_name;
        private String as_domain;
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

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonPropertyOrder({
            "analysisId",
            "errorLineCount",
            "errorLines"
    })
    public static class PostLog{
        private long analysisId;
        private int errorLineCount;
        private List<String> errorLines = new ArrayList<>();
    }

    @Data
    public static class IpInfoResponse{
        private String ip;
        private String asn;
        private String as_name;
        private String as_domain;
        private String country_code;
        private String country;
        private String continent_code;
        private String continent;
    }
}
