package com.thoughtworks.martdhis2sync.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Mapping {
    MappingJson mappingJson;
    Config config;

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
}
