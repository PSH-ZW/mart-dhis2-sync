package com.thoughtworks.martdhis2sync.processor;

import com.google.gson.JsonObject;
import com.thoughtworks.martdhis2sync.dao.EnrollmentDAO;
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
               "2021-09-28T09:45:20.373",
               "2021-09-28T09:45:20.373",
               "ACTIVE",
               "",
               events
        );
    }

    Event getEvent(JsonObject tableRow, JsonObject mapping) {
        String instanceId = patientDAO.getInstanceIdForPatient(tableRow.get("patient_id").getAsString());
        return new Event(
                "",
                instanceId,
                enrollmentDAO.getEnrollmentIdForInstanceId(instanceId),
                programId,
                mapping.get(Constants.DHIS_PROGRAM_STAGE_ID).getAsString(),
                orgUnitId,
                "2021-10-29T09:22:03.510",
                Event.ACTIVE,
                "TestId",
                getDataValues(tableRow, mapping)
        );
    }
}
