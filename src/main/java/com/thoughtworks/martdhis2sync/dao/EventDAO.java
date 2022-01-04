package com.thoughtworks.martdhis2sync.dao;

import com.thoughtworks.martdhis2sync.model.AnalyticsCronJob;
import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
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

    public List<DhisSyncEvent> getEventsToSync() {
        String sql = "SELECT id, program_id, encounter_id, patient_id, user, comment from events_to_sync where synced = false";
        List<DhisSyncEvent> value = jdbcTemplate.query(sql, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(DhisSyncEvent.class));
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
}
