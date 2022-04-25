package com.thoughtworks.martdhis2sync.mapper;

import com.thoughtworks.martdhis2sync.util.BatchUtil;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class PsiRowMapper extends ColumnMapRowMapper {
    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Object> mapOfColValues = this.createColumnMap(columnCount);

        for(int i = 1; i <= columnCount; ++i) {
            String key = this.getColumnKey(JdbcUtils.lookupColumnName(rsmd, i));
            //Appends table name to columns to make them unique as some tables were having columns of same name,
            //thus leading to conflicts while resolving the DHIS data element id corresponding to the column.
            String tableName = rsmd.getTableName(i);
            if(BatchUtil.shouldAppendTableNameToColumnName(tableName, key)) {
                key = tableName + "_" + key;
            }
            Object obj = this.getColumnValue(rs, i);
            mapOfColValues.put(key, obj);
        }

        return mapOfColValues;
    }
}
