package com.thoughtworks.martdhis2sync.writer;

import com.thoughtworks.martdhis2sync.model.*;
import com.thoughtworks.martdhis2sync.repository.SyncRepository;
import com.thoughtworks.martdhis2sync.responseHandler.EnrollmentResponseHandler;
import com.thoughtworks.martdhis2sync.responseHandler.EventResponseHandler;
import com.thoughtworks.martdhis2sync.service.JobService;
import com.thoughtworks.martdhis2sync.util.BatchUtil;
import com.thoughtworks.martdhis2sync.util.EventUtil;
import com.thoughtworks.martdhis2sync.util.TEIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.thoughtworks.martdhis2sync.util.BatchUtil.removeLastChar;

@Component
@StepScope
public class NewActiveAndCompletedEnrollmentWithEventsWriter implements ItemWriter<ProcessedTableRow> {
    private static final String URI = "/api/enrollments?strategy=CREATE_AND_UPDATE";

    @Autowired
    private SyncRepository syncRepository;

    @Autowired
    private EnrollmentResponseHandler enrollmentResponseHandler;

    @Autowired
    private EventResponseHandler eventResponseHandler;

    @Value("#{jobParameters['openLatestCompletedEnrollment']}")
    private String openLatestCompletedEnrollment;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String LOG_PREFIX = "NEW COMPLETED ENROLLMENT WITH EVENTS SYNC: ";
    private static final String YES = "yes";
    private static final String NO = "no";

    private List<EventTracker> eventTrackers = new ArrayList<>();

    private static final String EVENT_API_FORMAT = "{" +
                "\"event\":\"%s\", " +
                "\"trackedEntityInstance\":\"%s\", " +
                "\"enrollment\":\"%s\", " +
                "\"program\":\"%s\", " +
                "\"programStage\":\"%s\", " +
                "\"orgUnit\":\"%s\", " +
                "\"eventDate\":\"%s\", " +
                "\"status\":\"%s\", " +
                "\"dataValues\":[%s]" +
            "}";

    private static final String ENROLLMENT_API_FORMAT = "{" +
                "\"enrollment\":\"%s\", " +
                "\"trackedEntityInstance\":\"%s\", " +
                "\"orgUnit\":\"%s\", " +
                "\"program\":\"%s\", " +
                "\"enrollmentDate\":\"%s\", " +
                "\"incidentDate\":\"%s\", " +
                "\"status\":\"%s\", " +
                "\"events\":[%s]" +
            "}";

    @Override
    public void write(List<? extends ProcessedTableRow> tableRows) throws Exception {
        eventTrackers.clear();
        Map<String, EnrollmentAPIPayLoad> groupedEnrollmentPayLoad = getGroupedEnrollmentPayLoad(tableRows);
        Collection<EnrollmentAPIPayLoad> payLoads = groupedEnrollmentPayLoad.values();
        String apiBody = getAPIBody(groupedEnrollmentPayLoad);
        ResponseEntity<DHISEnrollmentSyncResponse> enrollmentResponse = syncRepository.sendEnrollmentData(URI, apiBody);
        processResponseEntity(enrollmentResponse, payLoads);
    }

    private void processResponseEntity(ResponseEntity<DHISEnrollmentSyncResponse> responseEntity, Collection<EnrollmentAPIPayLoad> payLoads) {
        Iterator<EnrollmentAPIPayLoad> iterator = payLoads.iterator();
        List<EnrollmentImportSummary> enrollmentImportSummaries = responseEntity.getBody().getResponse().getImportSummaries();
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            enrollmentResponseHandler.processImportSummaries(enrollmentImportSummaries, iterator);
            eventResponseHandler.process(payLoads, enrollmentImportSummaries, eventTrackers, logger, LOG_PREFIX);
        } else {
            JobService.setIS_JOB_FAILED(true);
            enrollmentResponseHandler.processErrorResponse(enrollmentImportSummaries, iterator, logger, LOG_PREFIX);
            eventResponseHandler.process(payLoads, enrollmentImportSummaries, eventTrackers, logger, LOG_PREFIX);
        }
    }

    private Map<String, EnrollmentAPIPayLoad> getGroupedEnrollmentPayLoad(List<? extends ProcessedTableRow> tableRows) {
        Map<String, EnrollmentAPIPayLoad> groupedEnrollments = new HashMap<>();
        tableRows.forEach(row -> {
            if (groupedEnrollments.containsKey(row.getProgramUniqueId())) {
                EnrollmentAPIPayLoad enrollmentAPIPayLoad = groupedEnrollments.get(row.getProgramUniqueId());
                List<Event> events = row.getPayLoad().getEvents();
                if (!events.isEmpty()) {
                    enrollmentAPIPayLoad.getEvents().add(events.get(0));
                }
            } else {
                groupedEnrollments.put(row.getProgramUniqueId(), row.getPayLoad());
            }
        });

        return groupedEnrollments;
    }

    private String getAPIBody(Map<String, EnrollmentAPIPayLoad> groupedEnrollmentPayLoad) {
        StringBuilder body = new StringBuilder();

        groupedEnrollmentPayLoad.forEach((key, value) -> {
            List<Event> events = value.getEvents();
            body
                .append(String.format(
                        ENROLLMENT_API_FORMAT,
                        getEnrollmentId(value),
                        value.getInstanceId(),
                        value.getOrgUnit(),
                        value.getProgram(),
                        value.getProgramStartDate(),
                        value.getIncidentDate(),
                        EnrollmentAPIPayLoad.STATUS_ACTIVE,
                        getEventBody(events)
                ))
                .append(",");

            eventTrackers.addAll(EventUtil.getEventTrackers(events));
        });

        return String.format("{\"enrollments\":[%s]}", removeLastChar(body));
    }

    private String getEventBody(List<Event> events) {
        StringBuilder eventsApiBuilder = new StringBuilder();
        events.forEach(event -> {
            eventsApiBuilder
                .append(String.format(EVENT_API_FORMAT,
                        event.getEvent(),
                        event.getTrackedEntityInstance(),
                        event.getEnrollment(),
                        event.getProgram(),
                        event.getProgramStage(),
                        event.getOrgUnit(),
                        event.getEventDate(),
                        event.getStatus(),
                        getDataValues(event.getDataValues())
                ))
                .append(",");
        });

        return removeLastChar(eventsApiBuilder);
    }

    private String getDataValues(Map<String, String> dataValues) {
        StringBuilder dataValuesApiBuilder = new StringBuilder();
        dataValues.forEach((key, value) -> dataValuesApiBuilder.append(
                String.format("{\"dataElement\":\"%s\", \"value\":\"%s\"},", key, BatchUtil.getEscapedString(value))
        ));

        return removeLastChar(dataValuesApiBuilder);
    }

    private String getEnrollmentId(EnrollmentAPIPayLoad enrollment) {
        String enrollmentId = enrollment.getEnrollmentId();
        if(!StringUtils.isEmpty(enrollmentId)) {
            return enrollmentId;
        }
        List<Enrollment> enrollmentDetails = TEIUtil.getInstancesWithEnrollments().get(enrollment.getInstanceId());
        if (null == enrollmentDetails || enrollmentDetails.isEmpty()) {
            return "";
        }
        return getActiveEnrollmentId(enrollmentDetails);
    }

    private String getActiveEnrollmentId(List<Enrollment> enrollments) {
        Optional<Enrollment> activeEnrollment = enrollments.stream()
                .filter(enrollment -> EnrollmentAPIPayLoad.STATUS_ACTIVE.equals(enrollment.getStatus()))
                .findFirst();

        return activeEnrollment.isPresent() ? activeEnrollment.get().getEnrollment() : "";
    }
}
