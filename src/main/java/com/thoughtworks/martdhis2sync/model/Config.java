package com.thoughtworks.martdhis2sync.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private List<String> searchable;
    private List<String> comparable;
    private String openLatestCompletedEnrollment;
}
