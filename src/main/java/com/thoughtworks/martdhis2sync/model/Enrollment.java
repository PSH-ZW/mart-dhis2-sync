package com.thoughtworks.martdhis2sync.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Enrollment {
    private String program;
    private String enrollment;
    private String orgUnit;
    private String enrollmentDate;
    private String completedDate;
    private String status;
}
