package com.wemadetest.accessanalyzer.repository;

import com.wemadetest.accessanalyzer.entity.DetailLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DetailLogRepository {
    private AtomicLong id = new AtomicLong(-1);
    private final Map<Long, DetailLog> detailLogDatabase = new ConcurrentHashMap<Long, DetailLog>();
    private final AccessLogRepository accessLogRepository;

    public DetailLogRepository(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    public void saveAll(List<DetailLog> detailLog, long analysisId) {
        for(DetailLog logs : detailLog) {
            detailLogDatabase.put(id.incrementAndGet(), logs);
            accessLogRepository.get(analysisId).getDetailLogs().add(logs);
        }
    }
}
