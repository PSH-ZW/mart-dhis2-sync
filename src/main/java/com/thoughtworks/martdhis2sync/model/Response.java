package com.thoughtworks.martdhis2sync.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private String responseType;
    private String status;
    private int imported;
    private int updated;
    private int deleted;
    private int ignored;
    private List<ImportSummary> importSummaries;
    private int total;
}
