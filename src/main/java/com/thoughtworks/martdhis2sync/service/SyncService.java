package com.thoughtworks.martdhis2sync.service;

import com.google.gson.Gson;
import com.thoughtworks.martdhis2sync.dao.EventDAO;
import com.thoughtworks.martdhis2sync.model.Config;
import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
import com.thoughtworks.martdhis2sync.model.MappingJson;
import com.thoughtworks.martdhis2sync.util.TEIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.Collections;
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
            //TODO: log proper program name
            loggerService.addLog(syncEvent.getProgramId(), syncEvent.getUser(), syncEvent.getComment());

            try {
                //TODO:get it as an object.
                Map<String, Object> mapping = mappingService.getMapping(syncEvent.getProgramId());
                Gson gson = new Gson();
                MappingJson mappingJson = gson.fromJson(mapping.get("mapping_json").toString(), MappingJson.class);
                //TODO:Use proper searchable and comparable
                Config config = gson.fromJson(mapping.get("config").toString(), Config.class);
                //TODO: clearing the global maps, we need to handle instance_tracking properly.
                TEIUtil.resetPatientTEIUidMap();
                TEIUtil.resetTrackedEntityInstaceIDs();
                teiService.getTrackedEntityInstances(syncEvent.getPatientId());
                teiService.triggerJob(syncEvent.getPatientId(), syncEvent.getUser(),
                        Collections.singletonList("uic"), new ArrayList<>());

                programDataSyncService.syncProgramDetails(syncEvent, mappingJson);
                loggerService.updateLog(syncEvent.getProgramId(), SUCCESS);
                eventDAO.markEventAsSynced(syncEvent.getId());
            } catch (HttpServerErrorException e) {
                loggerService.updateLog(syncEvent.getProgramId(), FAILED);
                throw e;
            } catch (Exception e) {
                loggerService.updateLog(syncEvent.getProgramId(), FAILED);
                e.printStackTrace();
            }
        }
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
