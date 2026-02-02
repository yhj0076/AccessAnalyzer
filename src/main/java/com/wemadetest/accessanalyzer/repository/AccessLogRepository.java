package com.wemadetest.accessanalyzer.repository;

import com.wemadetest.accessanalyzer.entity.AccessLog;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccessLogRepository {
    private long id = -1;
    private final Map<Long, AccessLog> accessLogDatabase = new ConcurrentHashMap<>();

    public long save(AccessLog accessLog) {
        accessLogDatabase.put(++id, accessLog);
        return id;
    };

    public boolean delete(long id) {
        return accessLogDatabase.remove(id) != null;
    }

    public AccessLog get(long id) {
        return accessLogDatabase.get(id);
    }
}
