package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.step.EventStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.SyncFailedException;

@Component
public class EventService {

    @Autowired
    private JobService jobService;

    @Autowired
    private EventStep eventStep;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String LOG_PREFIX = "Event Service: ";
    private static final String PE_JOB_NAME = "Sync Event";


    public void triggerJob(String programName, String user, String lookupTable, Object eventMapping)
            throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException, SyncFailedException {

        try {
            jobService.triggerJob(programName, user, lookupTable, PE_JOB_NAME, eventStep, eventMapping);
        } catch (Exception e) {
            logger.error(LOG_PREFIX + e.getMessage());
            throw e;
        }
    }
}
