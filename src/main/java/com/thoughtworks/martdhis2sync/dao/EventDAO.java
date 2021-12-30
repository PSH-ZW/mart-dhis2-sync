package com.thoughtworks.martdhis2sync.dao;

import com.thoughtworks.martdhis2sync.model.AnalyticsCronJob;
import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventDAO {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Value("${sync.cronJob.name}")
    private String syncCronJobName;

    public AnalyticsCronJob getSyncCronJob() {
        String sql = "SELECT * FROM analytics_cron_job WHERE name = ? and enabled = true";
        List<AnalyticsCronJob> syncCronJob =  jdbcTemplate.query(sql, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(AnalyticsCronJob.class), syncCronJobName);
        if(!CollectionUtils.isEmpty(syncCronJob)) {
            return syncCronJob.get(0);
        }
        return null;
    }

    public List<DhisSyncEvent> getEventsToSync() {
        String sql = "SELECT program_id, encounter_id, patient_id, user, comment from events_to_sync where synced = false";
        List<DhisSyncEvent> value = jdbcTemplate.query(sql, JdbcTemplateMapperFactory.newInstance()
                .newRowMapper(DhisSyncEvent.class));
        if(!CollectionUtils.isEmpty(value)) {
            return value;
        }
        return new ArrayList<>();
    }
}
