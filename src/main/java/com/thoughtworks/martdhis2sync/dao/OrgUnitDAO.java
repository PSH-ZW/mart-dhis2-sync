package com.thoughtworks.martdhis2sync.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrgUnitDAO {

    @Autowired
    @Qualifier("jdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(OrgUnitDAO.class);

    public String getOrgUnitNameByID(String orgUnitUID) {
        String sql = "select orgunit from orgunit_tracker where id = ?";
        try{
            return jdbcTemplate.queryForObject(sql, String.class, orgUnitUID);
        } catch (DataAccessException e) {
            logger.info(String.format("Could not find org_unit with id %s!!", orgUnitUID));
        }
        return "";
    }

}
