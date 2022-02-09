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
            return jdbcTemplate.queryForObject(String.format(sql, instanceId), String.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean enrolmentExistsInTracker(String enrolmentId) {
        String sql = "select count(*) from enrolment_tracker where enrolment_id = '%s'";
        int count = jdbcTemplate.queryForObject(String.format(sql, enrolmentId), Integer.class);
        return count > 0;
    }

    public void insertIntoEnrollmentTracker(EnrollmentAPIPayLoad enrollment) {
        String sql = "insert into enrolment_tracker(instance_id, enrolment_id) values(?, ?)";
        try{
            jdbcTemplate.update(sql, enrollment.getInstanceId(), enrollment.getEnrollmentId());
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
