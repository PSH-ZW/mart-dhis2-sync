package com.thoughtworks.martdhis2sync.dao;

import com.thoughtworks.martdhis2sync.model.EnrollmentAPIPayLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import static com.thoughtworks.martdhis2sync.util.BatchUtil.*;

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

    public String getOldestProgramEnrolmentDateForPatient(Integer patientId) {
        // Return the oldest program enrollment date for patient. If patient does not have any enrollments take the
        // patient creation date as enrollment date.
        String enrollmentDateSql = "select date_enrolled from program_enrolment where patient_id = ? order by date_enrolled limit 1";
        String patientCreatedDateSql = "select date_created from patient where patient_id = ?";
        String oldestEnrolmentDate;
        try{
            return jdbcTemplate.queryForObject(enrollmentDateSql, String.class, patientId);
        } catch (DataAccessException e) {
            logger.error("Could not get enrollment date for patient with id {}, getting date_created for patient", patientId);
        }
        try{
            oldestEnrolmentDate = jdbcTemplate.queryForObject(patientCreatedDateSql, String.class, patientId);
            return getFormattedDateString(oldestEnrolmentDate, DATEFORMAT_WITH_24HR_TIME, DATEFORMAT_WITHOUT_TIME);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return "";
    }
}
