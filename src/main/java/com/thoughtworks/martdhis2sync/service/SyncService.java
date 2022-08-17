package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.dao.EventDAO;
import com.thoughtworks.martdhis2sync.model.Config;
import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
import com.thoughtworks.martdhis2sync.model.Mapping;
import com.thoughtworks.martdhis2sync.model.MappingJson;
import com.thoughtworks.martdhis2sync.util.TEIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;

import static com.thoughtworks.martdhis2sync.service.LoggerService.*;

@Service
public class SyncService {

    @Autowired
    protected TEIService teiService;

    @Autowired
    protected EnrollmentService enrollmentService;

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
            }
            lastProcessedEventId = eventsToSync.get(eventsToSync.size() - 1).getId();
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
            String patientId = event.getPatientId();
            teiService.getTrackedEntityInstances(patientId);
            teiService.triggerJob(patientId, event.getUserName(),
                    config.getSearchable(), config.getComparable());
            //if patient doesn't have enrollment, check if there is an enrollment for the patient in DHIS created from another orgunit.
            if(!enrollmentService.enrollmentExistsInTracker(patientId)) {
                //If an enrollment is present in DHIS, insert it into enrollment_tracker.
                enrollmentService.addEnrollmentToTrackerIfEnrollmentExistsInDhis(patientId);
            }
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
        //For some patients, somehow the orgunit is getting set as null while flattening.
        //This is a workaround for setting the those orgunits.
        Map<String, String> invalidPatients = teiService.verifyOrgUnitsForPatients();
        if (invalidPatients.size() > 0) {
            teiService.setDefaultOrgUnitForPatientsWithInvalidOrgUnit(invalidPatients.keySet());
        }
    }
}
