package com.wemadetest.accessanalyzer.controller;

import com.wemadetest.accessanalyzer.dto.AnalysisDto;
import com.wemadetest.accessanalyzer.entity.AccessLog;
import com.wemadetest.accessanalyzer.entity.DetailLog;
import com.wemadetest.accessanalyzer.parser.LogParser;
import com.wemadetest.accessanalyzer.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "분석 API", description = "로그 업로드 및 분석 API")
@RestController
@Slf4j
@RequestMapping("/analysis")
@AllArgsConstructor
public class AnalysisController {
    private final LogParser logParser;
    private final AnalysisService analysisService;

    @Operation(
            summary = "로그 업로드 및 분석 실행",
            description = "10MB 이하의 .csv 파일을 업로드했을 때 분석 ID와 에러 발생 라인을 확인하는 기능",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AnalysisDto.PostLog.class),
                                    examples = {@ExampleObject(
                                            name = "에러 라인 없는 응답 성공 시 출력",
                                            summary = "성공 응답 예시",
                                            value = """
                                                    {
                                                        "analysisId": 0,
                                                        "errorLineCount": 0,
                                                        "errorLines": []
                                                    }
                                                    """
                                    ),
                                            @ExampleObject(
                                                    name = "에러 라인 있는 응답 성공 시 출력",
                                                    summary = "에러 라인 포함 응답 예시",
                                                    value = """
                                                            {
                                                                "analysisId": 0,
                                                                "errorLineCount": 3,
                                                                "errorLines": [
                                                                    "\\"1/29/2026, 5:44:17.000 AM\\",211.226.154.23,GET,/event/banner/mir2/popup,MyThreadedApp/1.0,200,HTTP/1.1,176,1138,0TLSv1.2,/event/banner/mir2/popup",
                                                                    "\\"1/29/2026, 5:44:17.000 AM\\",221.210.222.59,GET,/event/banner/mir2/popup,MyThreadedApp/1.0,403,HTTP/1.1,176,1944,0TLSv1.3,/event/banner/mir2/popup",
                                                                    "\\"1/29/2026, 5:44:17.000 AM\\",20.249.183.75,GET,/main/main_mir.asp,Blackbox Exporter/0.24.0,200HTTP/1.1,120,43763,0,TLSv1.3,/main/main_mir.asp"
                                                                ]
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "200 이외 코드",
                            description = "실패",
                            content = {@Content(
                                    mediaType = "string",
                                    examples = {
                                            @ExampleObject(
                                                    name = "현재 업로드한 파일이 .csv가 아니다.",
                                                    summary = "csv파일이 아닐 때 응답 예시",
                                                    value = """
                                                            File is not csv
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "현재 파일에 내용이 비어있다.",
                                                    summary = "파일이 비어있을 때 응답 예시",
                                                    value = """
                                                            File is empty
                                                            """
                                            ),
                                    }
                            ),
                            @Content(
                                    mediaType = "application/json"
                            )}
                    )
            }
    )
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity postLog(@RequestParam("file") MultipartFile file){
        if(file.isEmpty()){
            return ResponseEntity.badRequest().body("File is empty");
        }

        if(!file.getOriginalFilename().endsWith(".csv")){
            return ResponseEntity.badRequest().body("File is not csv");
        }

        AnalysisDto.PostLog result = new AnalysisDto.PostLog();
        List<DetailLog> detailLogs = logParser.fileToDetailLog(file,result);
        long analysisId = analysisService.analyzeLog(detailLogs);
        result.setAnalysisId(analysisId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(
            summary = "분석 결과 조회",
            description = "분석 ID를 바탕으로 분석 결과를 조회하는 코드",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AnalysisDto.Response.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "상위 10개 응답 포함한 요약 통계. 모든 ip에 정상 응답한 상태이다.",
                                                    summary = "상위 10개 응답 포함한 요약 통계",
                                                    value = """
                                                            {
                                                                "requestCount": 1000,
                                                                "successRate": "63.30",
                                                                "redirectRate": "2.70",
                                                                "clientErrorRate": "32.60",
                                                                "serverErrorRate": "1.40",
                                                                "detailLogs": [
                                                                    {
                                                                        "clientIp": "121.158.115.86",
                                                                        "httpStatus": 200,
                                                                        "path": "/event/banner/mir2/popup",
                                                                        "country": "South Korea",
                                                                        "asn": "AS4766",
                                                                        "as_name": "Korea Telecom",
                                                                        "as_domain": "kt.com"
                                                                    },
                                                                    {
                                                                        "clientIp": "61.38.42.234",
                                                                        "httpStatus": 200,
                                                                        "path": "/bbs/list/mir2free",
                                                                        "country": "South Korea",
                                                                        "asn": "AS3786",
                                                                        "as_name": "LG DACOM Corporation",
                                                                        "as_domain": "lguplus.com"
                                                                    },
                                                                    {
                                                                        "clientIp": "112.144.4.88",
                                                                        "httpStatus": 200,
                                                                        "path": "/assets/Dormancy-D5QIuaVf.css",
                                                                        "country": "South Korea",
                                                                        "asn": "AS17858",
                                                                        "as_name": "LG POWERCOMM",
                                                                        "as_domain": "lguplus.co.kr"
                                                                    },
                                                                    {
                                                                        "clientIp": "112.144.4.88",
                                                                        "httpStatus": 200,
                                                                        "path": "/assets/Certification-DR2OXUGd.js",
                                                                        "country": "South Korea",
                                                                        "asn": "AS17858",
                                                                        "as_name": "LG POWERCOMM",
                                                                        "as_domain": "lguplus.co.kr"
                                                                    },
                                                                    {
                                                                        "clientIp": "112.144.4.88",
                                                                        "httpStatus": 200,
                                                                        "path": "/assets/M_Dormancy-BCv5Ajmt.js",
                                                                        "country": "South Korea",
                                                                        "asn": "AS17858",
                                                                        "as_name": "LG POWERCOMM",
                                                                        "as_domain": "lguplus.co.kr"
                                                                    },
                                                                    {
                                                                        "clientIp": "112.144.4.88",
                                                                        "httpStatus": 200,
                                                                        "path": "/memsec/dormancy/getDormancy",
                                                                        "country": "South Korea",
                                                                        "asn": "AS17858",
                                                                        "as_name": "LG POWERCOMM",
                                                                        "as_domain": "lguplus.co.kr"
                                                                    },
                                                                    {
                                                                        "clientIp": "211.178.107.103",
                                                                        "httpStatus": 200,
                                                                        "path": "/event/banner/mir2/popup",
                                                                        "country": "South Korea",
                                                                        "asn": "AS9318",
                                                                        "as_name": "SK Broadband Co Ltd",
                                                                        "as_domain": "skbroadband.com"
                                                                    },
                                                                    {
                                                                        "clientIp": "61.38.42.234",
                                                                        "httpStatus": 200,
                                                                        "path": "/bbs/list/mir2test",
                                                                        "country": "South Korea",
                                                                        "asn": "AS3786",
                                                                        "as_name": "LG DACOM Corporation",
                                                                        "as_domain": "lguplus.com"
                                                                    },
                                                                    {
                                                                        "clientIp": "61.38.42.234",
                                                                        "httpStatus": 200,
                                                                        "path": "/community/com_free_list.asp",
                                                                        "country": "South Korea",
                                                                        "asn": "AS3786",
                                                                        "as_name": "LG DACOM Corporation",
                                                                        "as_domain": "lguplus.com"
                                                                    },
                                                                    {
                                                                        "clientIp": "20.249.183.75",
                                                                        "httpStatus": 200,
                                                                        "path": "/event/echo/getMessage",
                                                                        "country": "South Korea",
                                                                        "asn": "AS8075",
                                                                        "as_name": "Microsoft Corporation",
                                                                        "as_domain": "microsoft.com"
                                                                    }
                                                                ]
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "상위 N개 응답 포함한 요약 통계. 최대 3초가 걸리며, 모든 IpInfo를 탐색하지 못할 수도 있다." +
                                                            " 이때, 동일한 N개의 응답을 보낼 시 IpInfo와 재통신하며 UNKNOWN이 순서대로 일부 복원된다.",
                                                    summary = "상위 N개 응답 포함한 요약 (IpInfo 통신 중단 및 실패)",
                                                    value = """
                                                            {
                                                                "requestCount": 1000,
                                                                "successRate": "63.30",
                                                                "redirectRate": "2.70",
                                                                "clientErrorRate": "32.60",
                                                                "serverErrorRate": "1.40",
                                                                "detailLogs": [
                                                                    {
                                                                        "clientIp": "121.158.115.86",
                                                                        "httpStatus": 200,
                                                                        "path": "/event/banner/mir2/popup",
                                                                        "country": "UNKNOWN",
                                                                        "asn": "UNKNOWN",
                                                                        "as_name": "UNKNOWN",
                                                                        "as_domain": "UNKNOWN"
                                                                    },
                                                                    {
                                                                        "clientIp": "61.38.42.234",
                                                                        "httpStatus": 200,
                                                                        "path": "/bbs/list/mir2free",
                                                                        "country": "UNKNOWN",
                                                                        "asn": "UNKNOWN",
                                                                        "as_name": "UNKNOWN",
                                                                        "as_domain": "UNKNOWN"
                                                                    },
                                                                    {
                                                                        "clientIp": "112.144.4.88",
                                                                        "httpStatus": 200,
                                                                        "path": "/assets/Dormancy-D5QIuaVf.css",
                                                                        "country": "UNKNOWN",
                                                                        "asn": "UNKNOWN",
                                                                        "as_name": "UNKNOWN",
                                                                        "as_domain": "UNKNOWN"
                                                                    }
                                                                ]
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "200 이외",
                            description = "실패",
                            content = @Content(
                                    mediaType = "application/json"
                            )
                    )
            }
    )
    @GetMapping("/{analysisId}")
    public ResponseEntity getResult(@PathVariable("analysisId") long analysisId,
                                    @RequestParam int limit){
        AccessLog accessLog = analysisService.getLog(analysisId);

        return new ResponseEntity<>(logParser.resultToResponse(accessLog,limit), HttpStatus.OK);
    }
}
