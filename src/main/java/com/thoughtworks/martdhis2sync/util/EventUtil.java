package com.thoughtworks.martdhis2sync.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thoughtworks.martdhis2sync.model.Event;
import com.thoughtworks.martdhis2sync.model.EventTracker;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.thoughtworks.martdhis2sync.util.BatchUtil.*;

public class EventUtil {

    public static Date date = new Date(Long.MIN_VALUE);

    @Getter
    private static List<EventTracker> existingEventTrackers = new ArrayList<>();

    @Getter
    @Setter
    private static List<String> elementsOfTypeDateTime;

    @Getter
    @Setter
    private static List<String> elementsOfTypeDate;

    @Getter
    private static List<EventTracker> newEventTrackers = new ArrayList<>();

    public static List<EventTracker> eventsToSaveInTracker = new ArrayList<>();

    private static Logger logger = LoggerFactory.getLogger(EventUtil.class.getName());

    public static List<EventTracker> getEventTrackers(List<Event> events) {
        return events.stream().map(event -> new EventTracker(
                event.getEvent(),
                event.getTrackedEntityInstance(),
                event.getProgram(),
                event.getEventUniqueId(),
                event.getProgramStage(),
                event.getEncounterId()
            )
        ).collect(Collectors.toList());
    }


    public static Map<String, String> getDataValues(JsonObject tableRow, JsonObject mapping) {
        Set<String> keys = tableRow.keySet();
        Map<String, String> dataValues = new HashMap<>();

        for (String key : keys) {
            JsonElement dataElement = mapping.get(key);
            if (hasValue(dataElement)) {
                String value = tableRow.get(key).getAsString();
                String dataElementInStringFormat = dataElement.getAsString();
                dataValues.put(
                        dataElementInStringFormat,
                        changeFormatIfDate(dataElementInStringFormat, value)
                );
            }
        }
        return dataValues;
    }

    private static String changeFormatIfDate(String elementId, String value) {
        logger.debug("Event Processor : changeFormatIfDate: " + elementId + ", " + value);
        if (getElementsOfTypeDate() != null && getElementsOfTypeDate().contains(elementId)) {
            String result =  BatchUtil.getDateOnly(value);
            logger.debug("Event Processor : (Date): " + result);
            return result;
        } else {
            if (getElementsOfTypeDateTime() != null && getElementsOfTypeDateTime().contains(elementId)) {
                String result = getFormattedDateString(
                        value,
                        DATEFORMAT_WITH_24HR_TIME,
                        DHIS_ACCEPTABLE_DATEFORMAT
                );
                logger.debug("Event Processor : (DateTime): " + result);
                return result;
            }
        }

        return value;
    }
}
