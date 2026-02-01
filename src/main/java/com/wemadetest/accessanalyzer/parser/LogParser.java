package com.wemadetest.accessanalyzer.parser;

import com.wemadetest.accessanalyzer.entity.DetailLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class LogParser {
    public List<DetailLog> fileToDetailLog(MultipartFile file){
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

                List<String> detailLogList = csvParser(log);

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

        return result;
    }
}