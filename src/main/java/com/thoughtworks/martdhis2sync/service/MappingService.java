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

    private Map<String, Mapping> mappingCache = new HashMap<>(); //used for caching mappings.
    private Map<String, String> dhisElementMap = new HashMap<>(); // used for displaying the elementName corresponding to the id in logs.

    public Mapping getMapping(String mappingName) {
        //TODO: check date modified and update mapping in case of mismatch.
        return mappingCache.computeIfAbsent(mappingName, this::getMappingAndAddToElementMap);
    }

    private Mapping getMappingAndAddToElementMap(String mappingName) {
       Map<String, Object> mappingData = mappingDAO.getMapping(mappingName);
       Mapping mapping = gson.fromJson(mappingData.toString(), Mapping.class);
       addToElementMap(mapping);
       return mapping;
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
