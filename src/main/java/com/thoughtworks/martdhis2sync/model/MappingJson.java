package com.thoughtworks.martdhis2sync.model;

import lombok.Data;

import java.util.Map;

@Data
public class MappingJson {
    //{form_table_name : {column_name : UID in DHIS2}}
    private Map<String, String> formTableMappings;
}
