package com.thoughtworks.martdhis2sync.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Constants {

    public static final String DHIS_PROGRAM_STAGE_ID = "dhisProgramStageId";
    public static final String PATIENT_TABLE_NAME = "patient";
    public static final String PATIENT_MAPPING_NAME = "Patient";


    public static final Integer DHIS_UID_LENGTH = 11;
    public static final String ATTRIBUTE_DATATYPE_MISMATCH = "Attribute.value";

    public static final Set<String> SPECIAL_FIELDS = Stream.of("encounter_id", "visit_id", "patient_id", "patient_identifier",
            "event_id", "enrollment_date_created", "program_stage", "date_created", "instance_id").collect(Collectors.toSet());
}
