package com.thoughtworks.martdhis2sync.dao;

import com.thoughtworks.martdhis2sync.model.AnalyticsCronJob;
import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
import com.thoughtworks.martdhis2sync.model.EventTracker;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventDAO {

    @Autowired
    @Qualifier("jdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    @Value("${sync.cronJob.name}")
    protected String syncCronJobName;

    private static final Logger logger = LoggerFactory.getLogger(EventDAO.class);

    public AnalyticsCronJob getSyncCronJob() {
        String sql = "SELECT * FROM analytics_cron_job WHERE name = ? and enabled = true";
        List<AnalyticsCronJob> syncCronJob = jdbcTemplate.query(sql, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(AnalyticsCronJob.class), syncCronJobName);
        if (!CollectionUtils.isEmpty(syncCronJob)) {
            return syncCronJob.get(0);
        }
        return null;
    }

    public List<DhisSyncEvent> getEventsToSync(int lastProcessedEventId, int limit) {
        String sql = "SELECT id, program_id, encounter_id, patient_id, username, comment from events_to_sync where synced = false and id > ? order by id limit ? ";
        List<DhisSyncEvent> value = jdbcTemplate.query(sql, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(DhisSyncEvent.class), lastProcessedEventId, limit);
        if (!CollectionUtils.isEmpty(value)) {
            return value;
        }
        return new ArrayList<>();
    }

    public void markEventAsSynced(Integer id) {
        String sql = "update events_to_sync set synced = true where id = ?";
        try {
            jdbcTemplate.update(sql, id);
        } catch (DataAccessException e) {
            logger.error(String.format("Could not mark event with id %s as synced", id), e);
        }
    }

    public boolean dataExistsInTableForEncounter(String tableName, String encounterId) {
        String sql = "select count(*) from %s where encounter_id = %s";
        int count = jdbcTemplate.queryForObject(String.format(sql, tableName, encounterId), Integer.class);
        return count > 0;
    }

    public boolean eventExistsInEventTracker(String eventId) {
        String sql = "select count(*) from event_tracker where event_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, eventId);
        return count > 0;
    }

    public void insertIntoEventTracker(EventTracker eventTracker) {
        String sql = "update event_tracker set event_id = ? where encounter_id = ? and program_stage = ?";
        try {
            jdbcTemplate.update(sql, eventTracker.getEventId(), eventTracker.getEncounterId(), eventTracker.getProgramStage());
        } catch (DataAccessException e) {
            //TODO: log using loggerService
            e.printStackTrace();
        }
    }

    public String getEventIdFromEventTrackerIfExists(Integer encounterId, String programStageId) {
        String sql = "select event_id from event_tracker where encounter_id = ? and program_stage = ?";
        String eventId = null;
        try {
            eventId = jdbcTemplate.queryForObject(sql, String.class, encounterId, programStageId);
        } catch (DataAccessException e) {
            logger.info(String.format("Could not find existing event_id for encounter %s and programStage : %s. Creating new enrolment.",
                    encounterId, programStageId));
        }
        return eventId != null ? eventId : "";
    }

    public String getEncounterOrgUnitId(Integer encounterId) {
        String sql = "select out.id from orgunit_tracker out inner join encounter e on e.org_unit = out.orgunit where encounter_id = ?";
        String orgUnitId = null;
        try {
            orgUnitId = jdbcTemplate.queryForObject(sql, String.class, encounterId);
        } catch (DataAccessException e) {
            logger.info("Could not find org unit for encounter {}.", encounterId);
        }
        return orgUnitId != null ? orgUnitId : "";
    }

    public int getRetryCountFromEventsToSync(Integer eventsToSyncID) {
        String checkSql = "select retry_count from events_to_sync where id = ?";
        List<Integer> count = jdbcTemplate.query(checkSql, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(Integer.class), eventsToSyncID);
        if (CollectionUtils.isEmpty(count) || count.get(0) == null) {
            return 0;
        }
        return count.get(0);
    }

    public void updateRetryCountForFailedSync(Integer id, Integer retryCount) {
        String sql = "update events_to_sync set retry_count = ? where id = ?";
        try {
            jdbcTemplate.update(sql, retryCount, id);
        } catch (DataAccessException e) {
            logger.error(String.format("Could not update retry_count for event with id %s as synced", id), e);
        }
    }
}
