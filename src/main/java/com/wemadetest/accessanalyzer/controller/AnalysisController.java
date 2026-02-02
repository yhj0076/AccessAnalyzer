package com.wemadetest.accessanalyzer.controller;

import com.wemadetest.accessanalyzer.entity.DetailLog;
import com.wemadetest.accessanalyzer.parser.LogParser;
import com.wemadetest.accessanalyzer.service.AnalysisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/analysis")
@AllArgsConstructor
public class AnalysisController {
    private final LogParser logParser;
    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity postLog(@RequestParam("file") MultipartFile file){
        if(file.isEmpty()){
            return ResponseEntity.badRequest().body("File is empty");
        }

        if(!file.getOriginalFilename().endsWith(".csv")){
            return ResponseEntity.badRequest().body("File is not csv");
        }

        List<DetailLog> detailLogs = logParser.fileToDetailLog(file);
        long analysisId = analysisService.analyzeLog(detailLogs);

        return new ResponseEntity<>(analysisId, HttpStatus.OK);
    }
}
