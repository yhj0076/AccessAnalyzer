package com.wemadetest.accessanalyzer.parser;

import com.wemadetest.accessanalyzer.dto.AnalysisDto;
import com.wemadetest.accessanalyzer.entity.AccessLog;
import com.wemadetest.accessanalyzer.entity.DetailLog;
import com.wemadetest.accessanalyzer.service.IpInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LogParser {
    private final IpInfoService ipInfoService;
    private final ExecutorService analysisExecutor;

    public LogParser(IpInfoService ipInfoService, ExecutorService analysisExecutor) {
        this.ipInfoService = ipInfoService;
        this.analysisExecutor = analysisExecutor;
    }

    public List<DetailLog> fileToDetailLog(MultipartFile file, AnalysisDto.PostLog postLog){
        List<DetailLog> detailLogs = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            DetailLog detailLog = new DetailLog();

            String log;

            boolean skipFirstLine = false;

            while ((log = reader.readLine()) != null){
                if(!skipFirstLine){
                    skipFirstLine = true;
                    continue;
                }

                detailLog = new DetailLog();
                List<String> detailLogList = csvParser(log);
                if(detailLogList.size() != 12){
                    postLog.getErrorLines().add(log);
                    continue;
                }

                detailLog.setTimeGeneratedUTC(stringToLocalDateTime(detailLogList.get(0)));
                detailLog.setClientIp(detailLogList.get(1));
                detailLog.setHttpMethod(detailLogList.get(2));
                detailLog.setRequestUri(detailLogList.get(3));
                detailLog.setUserAgent(detailLogList.get(4));
                detailLog.setHttpStatus(HttpStatus.resolve(Integer.parseInt(detailLogList.get(5))));
                detailLog.setHttpVersion(detailLogList.get(6));
                detailLog.setReceivedBytes(Integer.parseInt(detailLogList.get(7)));
                detailLog.setSentBytes(Integer.parseInt(detailLogList.get(8)));
                detailLog.setClientResponseTime(Float.parseFloat(detailLogList.get(9)));
                detailLog.setSslProtocol(detailLogList.get(10));
                detailLog.setOriginalRequestUriWithArgs(detailLogList.get(11));
                detailLogs.add(detailLog);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        postLog.setErrorLineCount(postLog.getErrorLines().size());
        return detailLogs;
    }

    public LocalDateTime stringToLocalDateTime(String dateTimeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy, h:mm:ss.SSS a", Locale.US);
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    public static List<String> csvParser(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean inString = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (c == ',' && !inString) {
                result.add(sb.toString());
                sb.setLength(0);
                continue;
            }

            sb.append(c);
        }

        result.add(sb.toString());

        //TODO: csv 라인 에러

        return result;
    }

    public AnalysisDto.Response resultToResponse(AccessLog accessLog, int limit){
        ExecutorService executor = analysisExecutor;

        AnalysisDto.Response response = new AnalysisDto.Response();

        double successRate = (double) accessLog.getSuccessCount() /accessLog.getDetailLogs().size()*100d;
        double redirectRate = (double) accessLog.getRedirectCount() /accessLog.getDetailLogs().size()*100d;
        double clientErrorRate = (double) accessLog.getClientErrorCount() /accessLog.getDetailLogs().size()*100d;
        double serverErrorRate = (double) accessLog.getServerErrorCount() /accessLog.getDetailLogs().size()*100d;

        response.setRequestCount(accessLog.getDetailLogs().size());

        response.setSuccessRate(String.format("%.2f", successRate));
        response.setRedirectRate(String.format("%.2f", redirectRate));
        response.setClientErrorRate(String.format("%.2f", clientErrorRate));
        response.setServerErrorRate(String.format("%.2f", serverErrorRate));

//        List<AnalysisDto.DetailLogResponse> detailLogResponseList = new ArrayList<>();
//        for(int i = 0; i < limit; i++){
//            DetailLog detailLog = accessLog.getDetailLogs().get(i);
//
//            AnalysisDto.DetailLogResponse detailLogResponse = new AnalysisDto.DetailLogResponse();
//            detailLogResponse.setPath(detailLog.getOriginalRequestUriWithArgs());
//            detailLogResponse.setClientIp(detailLog.getClientIp());
//            detailLogResponse.setHttpStatus(detailLog.getHttpStatus().value());
//
//            AnalysisDto.IpInfoResponse ipInfoResponse = ipInfoService.getIpInfo(detailLog.getClientIp());
//
//            detailLogResponse.setAsn(ipInfoResponse.getAsn());
//            detailLogResponse.setAs_name(ipInfoResponse.getAs_name());
//            detailLogResponse.setAs_domain(ipInfoResponse.getAs_domain());
//            detailLogResponse.setCountry(ipInfoResponse.getCountry());
//
//            detailLogResponseList.add(detailLogResponse);
//        }

        // ===== 병렬 IP 조회 =====
        List<CompletableFuture<AnalysisDto.DetailLogResponse>> futures =
                accessLog.getDetailLogs()
                        .stream()
                        .limit(limit)
                        .map(log ->
                                CompletableFuture.supplyAsync(() -> buildDetail(log), executor)
                                        .completeOnTimeout(
                                                buildFallback(log),
                                                3,
                                                TimeUnit.SECONDS
                                        )
                        )
                        .toList();

        // ===== 3초 제한 =====
        List<AnalysisDto.DetailLogResponse> detailLogResponseList =
                futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

        response.setDetailLogs(detailLogResponseList);

        return response;
    }

    private AnalysisDto.DetailLogResponse buildDetail(DetailLog log) {

        AnalysisDto.DetailLogResponse res =
                new AnalysisDto.DetailLogResponse();

        res.setPath(log.getOriginalRequestUriWithArgs());
        res.setClientIp(log.getClientIp());
        res.setHttpStatus(log.getHttpStatus().value());

        AnalysisDto.IpInfoResponse ip =
                ipInfoService.getIpInfo(log.getClientIp());

        res.setAsn(ip.getAsn());
        res.setAs_name(ip.getAs_name());
        res.setAs_domain(ip.getAs_domain());
        res.setCountry(ip.getCountry());

        return res;
    }

    private AnalysisDto.DetailLogResponse buildFallback(DetailLog log) {

        AnalysisDto.DetailLogResponse res =
                new AnalysisDto.DetailLogResponse();

        res.setPath(log.getOriginalRequestUriWithArgs());
        res.setClientIp(log.getClientIp());
        res.setHttpStatus(log.getHttpStatus().value());

        res.setAsn("UNKNOWN");
        res.setAs_name("UNKNOWN");
        res.setAs_domain("UNKNOWN");
        res.setCountry("UNKNOWN");

        return res;
    }

}