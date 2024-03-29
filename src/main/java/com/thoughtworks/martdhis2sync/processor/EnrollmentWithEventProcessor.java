package com.thoughtworks.martdhis2sync.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thoughtworks.martdhis2sync.model.EnrollmentAPIPayLoad;
import com.thoughtworks.martdhis2sync.model.Event;
import com.thoughtworks.martdhis2sync.model.ProcessedTableRow;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import static com.thoughtworks.martdhis2sync.util.BatchUtil.DATEFORMAT_WITH_24HR_TIME;

@Component
public abstract class EnrollmentWithEventProcessor {

    public ProcessedTableRow process(Object tableRow, Object mappingObj) {
        Gson gson = new GsonBuilder().setDateFormat(DATEFORMAT_WITH_24HR_TIME).create();
        JsonElement tableRowJsonElement = gson.toJsonTree(tableRow);
        JsonElement mappingObjJsonElement = gson.toJsonTree(mappingObj);

        JsonObject tableRowJsonObject = tableRowJsonElement.getAsJsonObject();
        JsonObject mappingJsonObject = mappingObjJsonElement.getAsJsonObject();

        Event event = getEvent(tableRowJsonObject, mappingJsonObject);
        List<Event> events = new LinkedList<>();
        if (event != null) {
            events.add(event);
        }
        EnrollmentAPIPayLoad enrollmentAPIPayLoad = getEnrollmentAPIPayLoad(tableRowJsonObject, events);

        return new ProcessedTableRow(
                "TestId", //TODO: This may not be required. Verify and remove this field.
                tableRowJsonObject.get("encounter_id").getAsInt(),
                enrollmentAPIPayLoad
        );
    }

    abstract Event getEvent(JsonObject tableRow, JsonObject mapping);
    abstract EnrollmentAPIPayLoad getEnrollmentAPIPayLoad(JsonObject tableRowJsonObject, List<Event> events);
}
