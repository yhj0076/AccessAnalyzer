package com.wemadetest.accessanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AnalysisDto {

    public static class DetailLog{
        private String path;
        private int httpStatus;
        private String clientIp;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Response{
        private int requestCount;
        private String successRate;
        private String redirectRate;
        private String clientErrorRate;
        private String serverErrorRate;
        private List<DetailLog> detailLogs;
    }
}
