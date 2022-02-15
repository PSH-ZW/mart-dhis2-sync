package com.thoughtworks.martdhis2sync.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessedTableRow {
    private String programUniqueId; //TODO: remove this
    private Integer encounterId;
    private EnrollmentAPIPayLoad payLoad;
}
