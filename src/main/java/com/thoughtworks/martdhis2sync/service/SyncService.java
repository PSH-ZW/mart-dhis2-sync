package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.dao.EventDAO;
import com.thoughtworks.martdhis2sync.model.Config;
import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
import com.thoughtworks.martdhis2sync.model.Mapping;
import com.thoughtworks.martdhis2sync.model.MappingJson;
import com.thoughtworks.martdhis2sync.util.TEIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.Map;

import static com.thoughtworks.martdhis2sync.service.LoggerService.FAILED;
import static com.thoughtworks.martdhis2sync.service.LoggerService.SUCCESS;

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

    public void syncToDhis() {
        validatePatientsBeforeSync();

        List<DhisSyncEvent> eventsToSync = eventDAO.getEventsToSync();
        for (DhisSyncEvent syncEvent : eventsToSync) {
            addSyncDetailsToLogComment(syncEvent);
            try {
                //TODO:get it as an object.
                Mapping mapping = mappingService.getMapping(syncEvent.getProgramId());
                MappingJson mappingJson = mapping.getMappingJson();
                Config config = mapping.getConfig();
                //TODO: clearing the global maps, we need to handle instance_tracking properly.
                TEIUtil.resetPatientTEIUidMap();
                TEIUtil.resetTrackedEntityInstaceIDs();
                //TODO: call this only if instanceId is not present in instance_tracker;
                teiService.getTrackedEntityInstances(syncEvent.getPatientId());
                teiService.triggerJob(syncEvent.getPatientId(), syncEvent.getUserName(),
                        config.getSearchable(), config.getComparable());

                programDataSyncService.syncProgramDetails(syncEvent, mappingJson);
                loggerService.updateLog(syncEvent.getId(), SUCCESS);
                eventDAO.markEventAsSynced(syncEvent.getId());
            } catch (HttpServerErrorException e) {
                loggerService.updateLog(syncEvent.getId(), FAILED);
                throw e;
            } catch (Exception e) {
                loggerService.updateLog(syncEvent.getId(), FAILED);
                e.printStackTrace();
            }
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
