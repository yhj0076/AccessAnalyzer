package com.wemadetest.accessanalyzer.service;

import com.wemadetest.accessanalyzer.entity.AccessLog;
import com.wemadetest.accessanalyzer.entity.DetailLog;
import com.wemadetest.accessanalyzer.repository.AccessLogRepository;
import com.wemadetest.accessanalyzer.repository.DetailLogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AnalysisService {
    private final AccessLogRepository accessLogRepository;
    private final DetailLogRepository detailLogRepository;

    public long analyzeLog(List<DetailLog> detailLogList){
        AccessLog accessLog = new AccessLog();

        int successRate = 0;
        int redirectRate = 0;
        int clientErrorRate = 0;
        int serverErrorRate = 0;
        for(DetailLog detailLog : detailLogList){
            if(detailLog.getHttpStatus().value()/100 == 2){
                successRate++;
            }
            else if(detailLog.getHttpStatus().value()/100 == 3){
                redirectRate++;
            }
            else if(detailLog.getHttpStatus().value()/100 == 4){
                clientErrorRate++;
            }
            else if(detailLog.getHttpStatus().value()/100 == 5){
                serverErrorRate++;
            }
        }

        accessLog.setSuccessCount(successRate);
        accessLog.setRedirectCount(redirectRate);
        accessLog.setClientErrorCount(clientErrorRate);
        accessLog.setServerErrorCount(serverErrorRate);

        long analysisId = accessLogRepository.save(accessLog);
        detailLogRepository.saveAll(detailLogList,analysisId);

        return analysisId;
    }

    public AccessLog getLog(long analysisId){
        return accessLogRepository.get(analysisId);
    }
}
