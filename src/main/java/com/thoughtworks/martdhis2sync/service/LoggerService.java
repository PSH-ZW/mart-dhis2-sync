package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.dao.LoggerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class LoggerService {

    @Autowired
    private LoggerDAO loggerDAO;

    public static final String CONTACT_ADMIN = "Please contact Admin team.";

    public static final String SUCCESS = "success";

    public static final String FAILED = "failed";

    private Set<String> logMessage = new LinkedHashSet<>();

    public void addLog(Integer syncEventId, String service, String user, String comments) {
        logMessage.clear();
        loggerDAO.addLog(syncEventId, service, user, comments);
    }

    public void updateLog(Integer syncEventId, String status) {
        if (FAILED.equalsIgnoreCase(status)) {
            logMessage.add(CONTACT_ADMIN);
        }
        String message = logMessage.toString();
        loggerDAO.updateLog(syncEventId, status, message.substring(1, message.length() - 1));
    }

    public void collateLogMessage(String message) {
        logMessage.add(message);
    }
}
