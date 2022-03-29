package com.thoughtworks.martdhis2sync.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MappingDAO {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Value("classpath:sql/Searchable.sql")
    private Resource searchableResource;

    public Map<String, Object> getMapping(String programName) {
        //adding double quotes to preserve case.
        String sql = String.format("SELECT mapping_json as \"mappingJson\", config FROM mapping WHERE program_name ='%s'", programName);

        return jdbcTemplate.queryForMap(sql);
    }
}
