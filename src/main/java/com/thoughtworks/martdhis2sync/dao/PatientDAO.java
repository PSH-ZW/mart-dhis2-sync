package com.thoughtworks.martdhis2sync.dao;

import com.thoughtworks.martdhis2sync.util.BatchUtil;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class PatientDAO {
    @Value("classpath:sql/DeltaEnrollmentInstances.sql")
    private Resource deltaEnrollmentInstances;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getDeltaEnrollmentInstanceIds(String enrollmentTable, String eventTable, String programName) throws Exception {
        String sql;
        try {
            sql = BatchUtil.convertResourceOutputToString(deltaEnrollmentInstances);
        } catch (IOException e) {
            throw new Exception("Error in converting sql to string:: " + e.getMessage());
        }

        return jdbcTemplate.queryForList(String.format(sql, enrollmentTable, programName, eventTable, enrollmentTable, programName));
    }

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
}
