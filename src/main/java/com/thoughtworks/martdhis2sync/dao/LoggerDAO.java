package com.thoughtworks.martdhis2sync.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.thoughtworks.martdhis2sync.util.BatchUtil.DATEFORMAT_WITH_24HR_TIME;
import static com.thoughtworks.martdhis2sync.util.BatchUtil.getDateFromString;
import static com.thoughtworks.martdhis2sync.util.BatchUtil.getStringFromDate;

@Component
public class LoggerDAO {
    @Autowired
    @Qualifier("namedJdbcTemplate")
    private NamedParameterJdbcTemplate parameterJdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String LOG_PREFIX = "LoggerDAO: ";
    private final String CATEGORY = "dhis-sync";
    public void addLog(Integer eventId, String service, String user, String comments) {
        String sql = "INSERT INTO log (program, event_id, synced_by, comments, status, status_info, date_created," +
                " category) VALUES (:service, :eventId, :user, :comments, 'pending', '', :dateCreated, :category);";
        String stringFromDate = getStringFromDate(new Date(), DATEFORMAT_WITH_24HR_TIME);
        Date dateFromString = getDateFromString(stringFromDate, DATEFORMAT_WITH_24HR_TIME);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("service", service);
        parameterSource.addValue("user", user);
        parameterSource.addValue("comments", comments);
        parameterSource.addValue("eventId", eventId);
        parameterSource.addValue("dateCreated", dateFromString);
        parameterSource.addValue("category", CATEGORY);

        int update = parameterJdbcTemplate.update(sql, parameterSource);

        if (update == 1) {
            logger.info(LOG_PREFIX + "Successfully inserted into log table");
        } else {
            logger.error(LOG_PREFIX + "Failed to insert into log table");
        }
    }

    public void updateLog(Integer eventId, String status, String statusInfo) {
        String sql = "UPDATE log SET status = :status, status_info = :statusInfo " +
                "WHERE event_id = :eventId AND status = 'pending';";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("status", status);
        parameterSource.addValue("statusInfo", statusInfo);
        parameterSource.addValue("eventId", eventId);

        int update = parameterJdbcTemplate.update(sql, parameterSource);

        if (update == 1) {
            logger.info("{} Successfully updated status of the event : {}", LOG_PREFIX, eventId);
        } else {
            logger.info("{} Failed updated status of the event {}", LOG_PREFIX, eventId);
        }
    }
}
