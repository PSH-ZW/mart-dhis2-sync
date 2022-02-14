package com.thoughtworks.martdhis2sync.model;

import lombok.Data;

import java.util.Map;

@Data
public class MappingJson {
    //TODO:can convert this to an object. Getting too nested here.
    //{formTableMappings : {form_table_name : {column_name : {id: 3UilOlsb>, displayName: Element name}}}, dhisProgramStageId : hpVv6E1Lzfl }
    private Map<String, Map<String, Map<String, String>>> formTableMappings;
    private String dhisProgramStageId;
}
