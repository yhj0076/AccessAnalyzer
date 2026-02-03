package com.wemadetest.accessanalyzer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AccessLog {
    private int successCount;
    private int redirectCount;
    private int clientErrorCount;
    private int serverErrorCount;

    private List<DetailLog> detailLogs = new ArrayList<>();
}
