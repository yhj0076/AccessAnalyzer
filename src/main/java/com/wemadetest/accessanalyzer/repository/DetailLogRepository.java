package com.wemadetest.accessanalyzer.repository;

import com.wemadetest.accessanalyzer.entity.DetailLog;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DetailLogRepository {
    private long id = 0;
    private final Map<Long, DetailLog> detailLogDatabase = new ConcurrentHashMap<Long, DetailLog>();
    private final AccessLogRepository accessLogRepository;

    public DetailLogRepository(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    public long save(DetailLog detailLog, long analysisId) {
        detailLogDatabase.put(id++, detailLog);
        accessLogRepository.get(analysisId).getDetailLogs().add(id);
        return id;
    }
}
