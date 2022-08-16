package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.dao.OrgUnitDAO;
import com.thoughtworks.martdhis2sync.dao.PatientDAO;
import com.thoughtworks.martdhis2sync.exception.PsiException;
import com.thoughtworks.martdhis2sync.model.Mapping;
import com.thoughtworks.martdhis2sync.model.MappingJson;
import com.thoughtworks.martdhis2sync.model.TrackedEntityInstanceInfo;
import com.thoughtworks.martdhis2sync.repository.SyncRepository;
import com.thoughtworks.martdhis2sync.step.TrackedEntityInstanceStep;
import com.thoughtworks.martdhis2sync.util.BatchUtil;
import com.thoughtworks.martdhis2sync.util.Constants;
import com.thoughtworks.martdhis2sync.util.TEIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.SyncFailedException;
import java.util.*;

@Component
public class TEIService {

    @Value("${country.org.unit.id.for.patient.data.duplication.check}")
    private String parentOrgUnitId;

    @Value("${country.org.unit.for.orgunit.sync}")
    private String currentOrgUnitId;

    @Value("${dhis.uic-duplication-check}")
    private Boolean performUicDuplicationCheck;

    @Value("${dhis2.program-id}")
    private String programId;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private LoggerService loggerService;

    private static final String TEI_URI = "/api/trackedEntityInstances?pageSize=10000";

    @Autowired
    private TrackedEntityInstanceStep trackedEntityInstanceStep;

    @Autowired
    private JobService jobService;

    @Autowired
    private OrgUnitDAO orgUnitDAO;

    @Autowired
    private PatientDAO patientDAO;

    @Autowired
    private SyncRepository syncRepository;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String LOG_PREFIX = "TEI Service: ";
    private static final String TEI_JOB_NAME = "Sync Tracked Entity Instance";

    public void triggerJob(String patientId, String user, List<String> searchableAttributes,
                           List<String> comparableAttributes)
            throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException, SyncFailedException {

        try {
            LinkedList<Step> steps = new LinkedList<>();
            Mapping mapping = mappingService.getMapping(Constants.PATIENT_MAPPING_NAME);
            MappingJson mappingJson = mapping.getMappingJson();
            Map<String, Map<String, String>> patientMappingWithElementNames =
                    mappingJson.getFormTableMappings().get(Constants.PATIENT_TABLE_NAME);
            Map<String, String> patientMapping = BatchUtil.getColumnNameToDhisElementIdMap(Constants.PATIENT_TABLE_NAME, patientMappingWithElementNames);
            steps.add(trackedEntityInstanceStep.get(patientId, patientMapping, searchableAttributes, comparableAttributes));
            jobService.triggerJob(user, TEI_JOB_NAME, steps, "");
        } catch (Exception e) {
            logger.error(LOG_PREFIX + e.getMessage());
            throw e;
        }
    }

    public void getTrackedEntityInstances(String patientId) {
        StringBuilder url = new StringBuilder();

        url.append(TEI_URI);
        url.append("&ou=");
        url.append(parentOrgUnitId);
        url.append("&ouMode=DESCENDANTS");
        url.append("&program=");
        url.append(programId);


        StringBuilder uri = new StringBuilder();
        uri.append("&filter=");
        uri.append("zRA08XEYiSF"); //TODO: DHIS id of uic hardcoded here, get it from mapping.
        uri.append(":IN:");

        List<String> uicForPatient = patientDAO.getUicForPatient(patientId);
        String patientUic = !CollectionUtils.isEmpty(uicForPatient) ? uicForPatient.get(0) : "";
        uri.append(patientUic);
        uri.append(";");
        uri.append("&includeAllAttributes=true");

        List<TrackedEntityInstanceInfo> allTEIInfos = syncRepository.getTrackedEntityInstances(url.toString() + uri).getBody().getTrackedEntityInstances();
        for(TrackedEntityInstanceInfo teiInfo : allTEIInfos){
            if(performUicDuplicationCheck && !teiInfo.getOrgUnit().equals(currentOrgUnitId)) {
                String orgUnitWithDuplicatePatient = orgUnitDAO.getOrgUnitNameByID(teiInfo.getOrgUnit());
                String errorMessage = String.format("Patient with UIC %s already exists under %s. Check and update UIC for patient in DHIS.",
                        patientUic, orgUnitWithDuplicatePatient);
                loggerService.collateLogMessage(errorMessage);
                throw new PsiException(errorMessage);
            }
        }
        TEIUtil.setTrackedEntityInstanceInfos(allTEIInfos);

        logger.info("TEIUtil.getTrackedEntityInstanceInfos().size(): {}", TEIUtil.getTrackedEntityInstanceInfos().size());
    }

    public Map<String, String> verifyOrgUnitsForPatients() {
        final String sql = "select \"patient_identifier\",\"org_unit\" from patient " +
                "where \"org_unit\" is null or " +
                "\"org_unit\" not in (select org_unit from  orgunit_tracker ot)";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<String, String> invalidPatients = new HashMap<>();
        rows.forEach(row -> {
            String patientID = (String) row.get("patient_identifier");
            String orgUnit = (String) row.get("org_unit");
            invalidPatients.put(patientID, orgUnit);
        });
        return invalidPatients;
    }

    public int setDefaultOrgUnitForPatientsWithInvalidOrgUnit(Set<String> patientIdentifiers) {
        final StringBuilder sql = new StringBuilder("update patient set org_unit = ? where patient_identifier in (");
        for(String identifier : patientIdentifiers) {
            sql.append("'").append(identifier).append("',");
        }
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append(")");
        return jdbcTemplate.update(sql.toString(), currentOrgUnitId);
    }


    public String getBahmniPatientIdentifier(String patientID) {
        return patientDAO.getBahmniPatientIdentifier(patientID);
    }
}

