package com.thoughtworks.martdhis2sync.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Mapping {
    MappingJson mappingJson;
    Config config;
    String dateModified;

    public MappingJson getMappingJson() {
        return mappingJson;
    }

    public void setMappingJson(MappingJson mappingJson) {
        this.mappingJson = mappingJson;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }
}
