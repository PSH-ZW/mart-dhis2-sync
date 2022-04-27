package com.thoughtworks.martdhis2sync.service;

import com.google.gson.Gson;
import com.thoughtworks.martdhis2sync.dao.MappingDAO;
import com.thoughtworks.martdhis2sync.model.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MappingService {

    @Autowired
    private MappingDAO mappingDAO;

    private static final Gson gson = new Gson();
    private static final String DATE_MODIFIED = "dateModified";

    private Map<String, Mapping> mappingCache = new HashMap<>(); //used for caching mappings.
    private Map<String, String> dhisElementMap = new HashMap<>(); // used for displaying the elementName corresponding to the id in logs.


    public Mapping getMapping(String mappingName) {
        Mapping mapping;
        if(mappingCache.containsKey(mappingName)) {
            mapping = mappingCache.get(mappingName);
            String currDateModified = mappingDAO.getDateModifiedForMapping(mappingName);
            if(!mapping.getDateModified().equals(currDateModified)) {
                mapping = getMappingAndAddToElementMap(mappingName);
                mappingCache.put(mappingName, mapping);
            }
        } else {
            mapping = mappingCache.computeIfAbsent(mappingName, this::getMappingAndAddToElementMap);
        }

        return mapping;
    }

    private Mapping getMappingAndAddToElementMap(String mappingName) {
       Map<String, Object> mappingData = mappingDAO.getMapping(mappingName);
       String dateModified = getQuotedDateModified(mappingName);
       mappingData.put(DATE_MODIFIED, dateModified);
       Mapping mapping = gson.fromJson(mappingData.toString(), Mapping.class);
       addToElementMap(mapping);
       return mapping;
    }

    private String getQuotedDateModified(String mappingName) {
        String dateModified = mappingDAO.getDateModifiedForMapping(mappingName);
        StringBuilder sb = new StringBuilder(dateModified);
        sb.insert(0, "\"");
        sb.append( "\"");
        return sb.toString();
    }

    private void addToElementMap(Mapping mapping) {
        for(Map<String, Map<String, String>> columnMappings : mapping.getMappingJson().getFormTableMappings().values()) {
            for(Map<String, String> elementMap : columnMappings.values()) {
                dhisElementMap.put(elementMap.get("id"), elementMap.get("displayName"));
            }
        }
    }

    public String getElementWithId(String elementId) {
        return dhisElementMap.get(elementId);
    }
}
