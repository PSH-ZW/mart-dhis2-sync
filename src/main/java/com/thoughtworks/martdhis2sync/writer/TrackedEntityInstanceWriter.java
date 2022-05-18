package com.thoughtworks.martdhis2sync.writer;

import com.thoughtworks.martdhis2sync.dao.PatientDAO;
import com.thoughtworks.martdhis2sync.model.DHISSyncResponse;
import com.thoughtworks.martdhis2sync.model.ImportSummary;
import com.thoughtworks.martdhis2sync.repository.SyncRepository;
import com.thoughtworks.martdhis2sync.service.LoggerService;
import com.thoughtworks.martdhis2sync.service.MappingService;
import com.thoughtworks.martdhis2sync.util.BatchUtil;
import com.thoughtworks.martdhis2sync.util.Constants;
import com.thoughtworks.martdhis2sync.util.MarkerUtil;
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

import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

import static com.thoughtworks.martdhis2sync.model.ImportSummary.IMPORT_SUMMARY_RESPONSE_SUCCESS;
import static com.thoughtworks.martdhis2sync.util.BatchUtil.getUnquotedString;
import static com.thoughtworks.martdhis2sync.util.MarkerUtil.CATEGORY_INSTANCE;

@Component
@StepScope
public class TrackedEntityInstanceWriter implements ItemWriter {

    private static final String EMPTY_STRING = "\"\"";
    private static Map<String, String> newTEIUIDs = new LinkedHashMap<>();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String LOG_PREFIX = "TEI SYNC: ";
    private boolean isSyncFailure;

    private static final String URI = "/api/trackedEntityInstances?strategy=CREATE_AND_UPDATE";

    @Value("#{jobParameters['user']}")
    private String user;

    @Autowired
    private PatientDAO patientDAO;

    @Autowired
    private SyncRepository syncRepository;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private MarkerUtil markerUtil;

    @Autowired
    private LoggerService loggerService;

    @Value("#{jobParameters['service']}")
    private String programName;

    private Iterator<Entry<String, String>> mapIterator;

    @Override
    public void write(List list) throws Exception {
        StringBuilder instanceApiFormat = new StringBuilder("{\"trackedEntityInstances\":[");
        list.forEach(item -> instanceApiFormat.append(item).append(","));
        instanceApiFormat.replace(instanceApiFormat.length() - 1, instanceApiFormat.length(), "]}");

        isSyncFailure = false;
        ResponseEntity<DHISSyncResponse> responseEntity = syncRepository.sendData(URI, instanceApiFormat.toString());

        mapIterator = TEIUtil.getPatientIdTEIUidMap().entrySet().iterator();
        newTEIUIDs.clear();
        TEIUtil.getTrackedEntityInstanceIDs().forEach((key, value) -> newTEIUIDs.put(getUnquotedString(key), getUnquotedString(value)));
        TEIUtil.resetTrackedEntityInstaceIDs();

        if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            processResponse(responseEntity.getBody().getResponse().getImportSummaries());
        } else {
            isSyncFailure = true;
            if (!StringUtils.isEmpty(responseEntity) && !StringUtils.isEmpty(responseEntity.getBody())) {
                processErrorResponse(responseEntity.getBody().getResponse().getImportSummaries());
            }
        }
        updateTracker();
        if (isSyncFailure) {
            throw new Exception();
        } else {
            //TODO: this can be removed.
            updateMarker();
        }
        TEIUtil.resetPatientTEIUidMap();
    }

    private void processErrorResponse(List<ImportSummary> importSummaries) {
        for (ImportSummary importSummary : importSummaries) {
            if (isConflicted(importSummary)) {
                importSummary.getConflicts().forEach(conflict -> {
                    String conflictType = conflict.getObject();
                    String errorMessage = conflict.getValue();
                    if(conflictType.equals(Constants.ATTRIBUTE_DATATYPE_MISMATCH)) {
                        String elementId = errorMessage.substring(errorMessage.length() - Constants.DHIS_UID_LENGTH);
                        String elementName = mappingService.getElementWithId(elementId);
                        logger.error("{} {} : {}",LOG_PREFIX, elementName, errorMessage);
                        loggerService.collateLogMessage(String.format("%s: %s", errorMessage.substring(0, errorMessage.length() - Constants.DHIS_UID_LENGTH), elementName));
                    }
                    else {
                        logger.error("{} {} : {}",LOG_PREFIX, conflictType, errorMessage);
                        loggerService.collateLogMessage(String.format("%s: %s", conflictType, errorMessage));
                    }
                });
                if (mapIterator.hasNext()) {
                    mapIterator.next();
                }
            } else {
                processResponse(Collections.singletonList(importSummary));
            }
        }
    }

    private void processResponse(List<ImportSummary> importSummaries) {
        importSummaries.forEach(importSummary -> {
            if (isImported(importSummary)) {
                while (mapIterator.hasNext()) {
                    Entry<String, String> entry = mapIterator.next();
                    if (EMPTY_STRING.equals(entry.getValue())) {
                        newTEIUIDs.put(getUnquotedString(entry.getKey()), importSummary.getReference());
                        break;
                    }
                }
            } else if (isConflicted(importSummary)) {
                isSyncFailure = true;
                importSummary.getConflicts().forEach(conflict -> {
                    logger.error(LOG_PREFIX + conflict.getValue());
                    loggerService.collateLogMessage(String.format("%s", conflict.getValue()));
                });
                if (mapIterator.hasNext()) {
                    mapIterator.next();
                }
            }
        });
    }

    private boolean isConflicted(ImportSummary importSummary) {
        return !importSummary.getConflicts().isEmpty();
    }

    private boolean isImported(ImportSummary importSummary) {
        return IMPORT_SUMMARY_RESPONSE_SUCCESS.equals(importSummary.getStatus()) && importSummary.getImportCount().getImported() == 1;
    }

    private void updateTracker() {
        if (!newTEIUIDs.isEmpty()) {
            int updateCount = 0;
            for (Entry<String, String> entry : newTEIUIDs.entrySet()) {
                String patientId =  entry.getKey();
                String instanceId = entry.getValue();
                Timestamp dateCreated = Timestamp.valueOf(BatchUtil.GetUTCDateTimeAsString());
                updateCount += patientDAO.insertIntoInstanceTracker(patientId, instanceId, user, dateCreated);
            }
            logger.info("{} Successfully inserted {}  TrackedEntityInstance UIDs.", LOG_PREFIX, updateCount);
        }
    }

    private void updateMarker() {
        markerUtil.updateMarkerEntry(programName, CATEGORY_INSTANCE,
                BatchUtil.getStringFromDate(TEIUtil.date, BatchUtil.DATEFORMAT_WITH_24HR_TIME));
    }
}
