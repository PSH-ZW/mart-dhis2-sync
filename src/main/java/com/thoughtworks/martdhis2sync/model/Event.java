package com.thoughtworks.martdhis2sync.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Event {
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String ACTIVE = "ACTIVE";

    private String event;
    private String trackedEntityInstance;
    private String enrollment;
    private String program;
    private String programStage;
    private String orgUnit;
    private String eventDate;
    private String status;
    private String eventUniqueId;
    private Integer encounterId;
    private Map<String, String> dataValues;
}
