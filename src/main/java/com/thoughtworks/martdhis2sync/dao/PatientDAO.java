package com.thoughtworks.martdhis2sync.dao;

import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.List;

@Component
public class PatientDAO {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    //TODO: could remove this join by saving the patient identifier in the form table instead of patient_id.
    public String getInstanceIdForPatient(String patientId) {
        String sql = "select i.instance_id from patient p inner join instance_tracker i on p.patient_identifier = i.patient_id" +
                " where p.patient_id = '%s'";
        List<String> value =  jdbcTemplate.query(String.format(sql, patientId),
                JdbcTemplateMapperFactory.newInstance().newRowMapper(String.class));
        if(!CollectionUtils.isEmpty(value)) {
            return value.get(0);
        }
        return null;
    }

    public List<String> getUicForPatient(String patientId) {
        String sql = "select uic from patient where patient_id = %s";
        return jdbcTemplate.query(String.format(sql, patientId),
                JdbcTemplateMapperFactory.newInstance().newRowMapper(String.class));
    }

    public String getBahmniPatientIdentifier(String patientId) {
        String sql = "select patient_identifier from patient where patient_id = %s";
        List<String> patientIdentifiers = jdbcTemplate.query(String.format(sql, patientId),
                JdbcTemplateMapperFactory.newInstance().newRowMapper(String.class));
        if(!CollectionUtils.isEmpty(patientIdentifiers)) {
            return patientIdentifiers.get(0);
        }
        return "";
    }

    public int insertIntoInstanceTracker(String patientId, String instanceId, String user, Timestamp dateCreated) {
        String sql = "INSERT INTO public.instance_tracker(patient_id, instance_id, created_by, date_created) values (? , ?, ?, ?)";
        try {
            return jdbcTemplate.update(sql, patientId, instanceId, user, dateCreated);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Possible duplicate patient record found! Another patient with the same UIC exists in DB.");
        }
        catch (DataAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
