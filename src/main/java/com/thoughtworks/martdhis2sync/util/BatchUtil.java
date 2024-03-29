package com.thoughtworks.martdhis2sync.util;

import com.google.gson.JsonElement;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class BatchUtil {

    private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATEFORMAT_WITH_24HR_TIME = "yyyy-MM-dd kk:mm:ss";
    public static final String DHIS_ACCEPTABLE_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String DATEFORMAT_WITHOUT_TIME = "yyyy-MM-dd";
    public static final String EMPTY_STRING = "\"\"";

    public static String convertResourceOutputToString(Resource resource) throws IOException {
        return IOUtils.toString(resource.getInputStream());
    }

    public static String getUnquotedString(String string) {
        return StringUtils.replace(string, "\"", "");
    }

    public static String getQuotedString(String string) {
        return "\"" + string + "\"";
    }

    public static String GetUTCDateTimeAsString() {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    public static String getStringFromDate(Date date, String format) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(format);
        return outputFormat.format(date);
    }

    public static Date getDateFromString(String date, String format) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(format);
        try {
            return outputFormat.parse(date);
        } catch (ParseException ignored) {

        }
        return new Date(Long.MIN_VALUE);
    }

    public static String getFormattedDateString(String date, String existingFormat, String expectedFormat) {
        return getStringFromDate(getDateFromString(date, existingFormat), expectedFormat);
    }

    public static boolean hasValue(JsonElement element) {
        return (null != element && !EMPTY_STRING.equals(element.toString()));
    }

    public static String removeLastChar(StringBuilder value) {
        int length = value.length();

        return length > 0 ? value.deleteCharAt(length - 1).toString() : "";
    }

    public static String getDateOnly(String dateString) {
        return getStringFromDate(getDateFromString(dateString, DATEFORMAT_WITHOUT_TIME), DATEFORMAT_WITHOUT_TIME);
    }

    public static String getEscapedString(String value) {
        return value.replace("\\","\\\\")
                    .replace("\"","\\\"");
    }

    public static Map<String, String> getColumnNameToDhisElementIdMap(String tableName, Map<String, Map<String, String>> mapping) {
        Map<String, String> columnToElementMapping = new HashMap<>();
        for(String columnName : mapping.keySet()) {
            String elementId = mapping.get(columnName).get("id");
            if(shouldAppendTableNameToColumnName(tableName, columnName)) {
                columnName = tableName + "_" + columnName;
            }
            columnToElementMapping.put(columnName, elementId);
        }

        return columnToElementMapping;
    }

    public static boolean shouldAppendTableNameToColumnName(String tableName, String columnName) {
        //Appending table names to columns to avoid conflicts in the case where there are same column names in different tables.
        //We will be skipping patient table as there won't be multiple tables for patient.
        return !tableName.equals(Constants.PATIENT_TABLE_NAME) && !Constants.SPECIAL_FIELDS.contains(columnName);
    }
}
