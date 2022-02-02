package com.thoughtworks.martdhis2sync.model;

import lombok.Data;

import java.util.Map;

@Data
public class MappingJson {
    //{formTableMappings : {form_table_name : {column_name : elementId in DHIS2}}, dhisProgramStageId : hpVv6E1Lzfl }
    private Map<String, Map<String, String>> formTableMappings;
    private String dhisProgramStageId;
}
