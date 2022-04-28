package com.thoughtworks.martdhis2sync.responseHandler;

import com.thoughtworks.martdhis2sync.model.*;
import com.thoughtworks.martdhis2sync.service.JobService;
import com.thoughtworks.martdhis2sync.service.LoggerService;
import com.thoughtworks.martdhis2sync.service.MappingService;
import com.thoughtworks.martdhis2sync.trackerHandler.TrackersHandler;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.thoughtworks.martdhis2sync.model.ImportSummary.IMPORT_SUMMARY_RESPONSE_ERROR;
import static com.thoughtworks.martdhis2sync.model.ImportSummary.IMPORT_SUMMARY_RESPONSE_SUCCESS;
import static com.thoughtworks.martdhis2sync.model.ImportSummary.IMPORT_SUMMARY_RESPONSE_WARNING;
import static com.thoughtworks.martdhis2sync.util.EventUtil.eventsToSaveInTracker;

@Component
public class EventResponseHandler {

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private TrackersHandler trackersHandler;

    public void process(Collection<EnrollmentAPIPayLoad> payLoads, List<EnrollmentImportSummary> importSummaries, List<EventTracker> eventTrackers, Logger logger, String logPrefix) {
        Iterator<EventTracker> eventTrackerIterator = eventTrackers.iterator();
        Iterator<EnrollmentAPIPayLoad> finalIterator = payLoads.iterator();
        importSummaries.forEach(summary -> {
            EnrollmentAPIPayLoad payLoad = finalIterator.next();
            Response eventsResponse = summary.getEvents();
            if (eventsResponse == null || eventsResponse.getImportSummaries() == null) {
                List<Event> events = payLoad.getEvents();
                events.forEach(i -> eventTrackerIterator.next());
            } else if (IMPORT_SUMMARY_RESPONSE_SUCCESS.equals(eventsResponse.getStatus())) {
                processImportSummaries(eventsResponse.getImportSummaries(), eventTrackerIterator);
            } else {
                JobService.setIS_JOB_FAILED(true);
                processErrorResponse(eventsResponse.getImportSummaries(), eventTrackerIterator, logger, logPrefix);
            }
        });
    }

    private void processImportSummaries(List<ImportSummary> importSummaries, Iterator<EventTracker> eventTrackerIterator) {
        importSummaries.forEach(importSummary -> {
            EventTracker eventTracker = eventTrackerIterator.next();
            if (isImported(importSummary)) {
                eventTracker.setEventId(importSummary.getReference());
                eventsToSaveInTracker.add(eventTracker);
                trackersHandler.addEventIdForEncounterAndProgramStage(eventTracker);
            }
        });
    }

    private void processErrorResponse(List<ImportSummary> importSummaries, Iterator<EventTracker> eventTrackerIterator, Logger logger, String logPrefix) {
        for (ImportSummary importSummary : importSummaries) {
            if (isIgnored(importSummary)) {
                eventTrackerIterator.next();
                String descriptionWithElementName = getInfoFromDescription(importSummary.getDescription());
                logger.error(logPrefix + descriptionWithElementName);
                loggerService.collateLogMessage(String.format("%s", importSummary.getDescription()));
            } else if (isConflicted(importSummary)) {
                importSummary.getConflicts().forEach(conflict -> {
                    String elementId = conflict.getObject();
                    String elementName = mappingService.getElementWithId(elementId);
                    logger.error("{} {} : {}",logPrefix, elementName, conflict.getValue());
                    loggerService.collateLogMessage(String.format("%s: %s", elementName, conflict.getValue()));
                });
                if(isImported(importSummary)) {
                    processImportSummaries(Collections.singletonList(importSummary), eventTrackerIterator);
                } else {
                    eventTrackerIterator.next();
                }
            } else {
                processImportSummaries(Collections.singletonList(importSummary), eventTrackerIterator);
            }
        }
    }

    //if string with id's included within '[',']' is passed ,will return string with the id name
    private String getInfoFromDescription(String description) {
        int startIndex = description.indexOf('[');
        int endIndex = description.indexOf(']');
        if(startIndex == 0) return description;
        String descriptionContainingId = description.substring(startIndex + 1, endIndex);
        StringBuilder stringBuilder  = new StringBuilder();
        stringBuilder.append(description, 0, startIndex + 1);
        String comma = "";
        String[] multipleId = descriptionContainingId.split(",");
        for(String Id: multipleId){
            stringBuilder.append(comma);
            comma = ",";
            String elementName = mappingService.getElementWithId(Id);
            if(elementName != null)  stringBuilder.append(elementName);
        }
        stringBuilder.append(description.substring(endIndex));
        return stringBuilder.toString();
    }


    private boolean isIgnored(ImportSummary importSummary) {
        return (IMPORT_SUMMARY_RESPONSE_ERROR.equals(importSummary.getStatus())
                || IMPORT_SUMMARY_RESPONSE_WARNING.equals(importSummary.getStatus()))
                && !StringUtils.isEmpty(importSummary.getDescription());
    }

    private boolean isConflicted(ImportSummary importSummary) {
        return (IMPORT_SUMMARY_RESPONSE_ERROR.equals(importSummary.getStatus())
                || IMPORT_SUMMARY_RESPONSE_WARNING.equals(importSummary.getStatus()))
                && !importSummary.getConflicts().isEmpty();
    }

    private boolean isImported(ImportSummary importSummary) {
        return (IMPORT_SUMMARY_RESPONSE_SUCCESS.equals(importSummary.getStatus())
                || IMPORT_SUMMARY_RESPONSE_WARNING.equals(importSummary.getStatus()))
                && importSummary.getImportCount().getImported() > 0;
    }
}
