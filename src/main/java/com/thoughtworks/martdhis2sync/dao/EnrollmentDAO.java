package com.thoughtworks.martdhis2sync.dao;

import com.thoughtworks.martdhis2sync.model.EnrollmentAPIPayLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentDAO {

    @Autowired
    @Qualifier("jdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(EventDAO.class);

    public String getEnrollmentIdForInstanceId(String instanceId) {
        String sql = "select enrolment_id from enrolment_tracker where instance_id = ?";
        try{
            return jdbcTemplate.queryForObject(sql, String.class, instanceId);
        } catch (DataAccessException e) {
            logger.info(String.format("Could not find existing enrolment for instance %s. Creating new enrolment.",
                    instanceId));
        }
        return "";
    }

    public boolean enrolmentExistsInEnrolmentTracker(String enrolmentId) {
        String sql = "select count(*) from enrolment_tracker where instance_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, enrolmentId);
        return count > 0;
    }

    public void insertIntoEnrollmentTracker(EnrollmentAPIPayLoad enrollment) {
        String sql = "insert into enrolment_tracker(instance_id, enrolment_id) values(?, ?)";
        try{
            jdbcTemplate.update(sql, enrollment.getInstanceId(), enrollment.getEnrollmentId());
        } catch (DataAccessException e) {
            //TODO: log using loggerService
            e.printStackTrace();
        }
    }
}
