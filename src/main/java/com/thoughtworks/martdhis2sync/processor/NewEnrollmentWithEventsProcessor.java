package com.thoughtworks.martdhis2sync.processor;

import com.google.gson.JsonObject;
import com.thoughtworks.martdhis2sync.dao.EnrollmentDAO;
import com.thoughtworks.martdhis2sync.dao.EventDAO;
import com.thoughtworks.martdhis2sync.dao.PatientDAO;
import com.thoughtworks.martdhis2sync.model.EnrollmentAPIPayLoad;
import com.thoughtworks.martdhis2sync.model.Event;
import com.thoughtworks.martdhis2sync.model.ProcessedTableRow;
import com.thoughtworks.martdhis2sync.util.Constants;
import lombok.Setter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.thoughtworks.martdhis2sync.util.EventUtil.getDataValues;

@Component
public class NewEnrollmentWithEventsProcessor extends EnrollmentWithEventProcessor implements ItemProcessor{

    @Value("${dhis2.program-id}")
    private String programId;

    @Value("${country.org.unit.id.for.patient.data.duplication.check}")
    private String orgUnitId;

    @Autowired
    protected PatientDAO patientDAO;

    @Autowired
    protected EnrollmentDAO enrollmentDAO;

    @Autowired
    protected EventDAO eventDAO;

    @Setter
    private Object mappingObj;

    @Override
    public ProcessedTableRow process(Object tableRow) {
        return super.process(tableRow, mappingObj);
    }

    EnrollmentAPIPayLoad getEnrollmentAPIPayLoad(JsonObject tableRowJsonObject, List<Event> events) {
        String instanceId = patientDAO.getInstanceIdForPatient(tableRowJsonObject.get("patient_id").getAsString());
        return new EnrollmentAPIPayLoad(
               enrollmentDAO.getEnrollmentIdForInstanceId(instanceId),
                instanceId,
               programId,
               orgUnitId,
               "2021-09-28T09:45:20.373",//TODO:add date started in enrolment_tracker.
               tableRowJsonObject.get("date_created").getAsString(),
               "ACTIVE",
               "",
               events
        );
    }

    Event getEvent(JsonObject tableRow, JsonObject mapping) {
        String instanceId = patientDAO.getInstanceIdForPatient(tableRow.get("patient_id").getAsString());
        String programStageId = mapping.get(Constants.DHIS_PROGRAM_STAGE_ID).getAsString();
        Integer encounterId = tableRow.get("encounter_id").getAsInt();
        return new Event(
                eventDAO.getEventIdFromEventTrackerIfExists(encounterId, programStageId),
                instanceId,
                enrollmentDAO.getEnrollmentIdForInstanceId(instanceId),
                programId,
                programStageId,
                orgUnitId,
                tableRow.get("date_created").getAsString(),
                Event.ACTIVE,
                "TestId",
                tableRow.get("encounter_id").getAsInt(),
                getDataValues(tableRow, mapping)
        );
    }
}
