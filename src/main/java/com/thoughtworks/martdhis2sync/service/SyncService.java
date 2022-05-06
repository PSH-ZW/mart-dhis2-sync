package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.dao.EventDAO;
import com.thoughtworks.martdhis2sync.model.Config;
import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
import com.thoughtworks.martdhis2sync.model.Mapping;
import com.thoughtworks.martdhis2sync.model.MappingJson;
import com.thoughtworks.martdhis2sync.util.TEIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.martdhis2sync.service.LoggerService.*;

@Service
public class SyncService {

    @Autowired
    protected TEIService teiService;

    @Autowired
    protected LoggerService loggerService;

    @Autowired
    protected ProgramDataSyncService programDataSyncService;

    @Autowired
    protected MappingService mappingService;

    @Autowired
    protected EventDAO eventDAO;

    @Value("${sync.page.size}")
    private Integer limit;

    public void syncToDhis() {
        validatePatientsBeforeSync();

        int lastProcessedEventId = 0;
        List<DhisSyncEvent> eventsToSync = eventDAO.getEventsToSync(lastProcessedEventId, limit);
        while(!CollectionUtils.isEmpty(eventsToSync)) {
            for (DhisSyncEvent event : eventsToSync) {
                addSyncDetailsToLogComment(event);
                syncEvent(event);
                lastProcessedEventId = event.getId();
            }
            eventsToSync = eventDAO.getEventsToSync(lastProcessedEventId, limit);
        }
    }

    private void syncEvent(DhisSyncEvent event) {
        try {
            Mapping mapping = mappingService.getMapping(event.getProgramId());
            MappingJson mappingJson = mapping.getMappingJson();
            Config config = mapping.getConfig();
            //TODO: clearing the global maps, we need to handle instance_tracking properly.
            TEIUtil.resetPatientTEIUidMap();
            TEIUtil.resetTrackedEntityInstaceIDs();
            //TODO: call this only if instanceId is not present in instance_tracker;
            teiService.getTrackedEntityInstances(event.getPatientId());
            teiService.triggerJob(event.getPatientId(), event.getUserName(),
                    config.getSearchable(), config.getComparable());

            programDataSyncService.syncProgramDetails(event, mappingJson);
            loggerService.updateLog(event.getId(), SUCCESS);
            eventDAO.markEventAsSynced(event.getId());
        } catch (ResourceAccessException re){
            loggerService.updateLog(event.getId(), CONNECTIVITY_ISSUE);
            re.printStackTrace();
        }catch (Exception e) {
            int retryCount = eventDAO.getRetryCountFromEventsToSync(event.getId());
            loggerService.updateLog(event.getId(), FAILED);
            eventDAO.updateRetryCountForFailedSync(event.getId(), retryCount + 1);
            e.printStackTrace();
        }
    }

    private void addSyncDetailsToLogComment(DhisSyncEvent syncEvent) {
        String patientId = teiService.getBahmniPatientIdentifier(syncEvent.getPatientId());
        String comment = String.format("Patient Id: %s  Encounter id :%s ", patientId, syncEvent.getEncounterId());
        loggerService.addLog(syncEvent.getId(), syncEvent.getProgramId(), syncEvent.getUserName(), comment);
    }

    private void validatePatientsBeforeSync() {
        Map<String, String> invalidPatients = teiService.verifyOrgUnitsForPatients();
        if (invalidPatients.size() > 0) {
            loggerService.collateLogMessage("Pre validation for sync service failed." +
                    " Invalid Org Unit specified for below patients. Update Patient Info in OpenMRS");
            invalidPatients.forEach((patientID, orgUnit) -> loggerService.collateLogMessage("[Patient ID (" + patientID + ") Org Unit ID (" + orgUnit + ")] "));
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Prevalidation for sync service failed." +
                    " Invalid Org Unit specified for below patients. Update Patient Info in OpenMRS, run Bahmni MART");
        }
    }
}
