package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.model.DhisSyncEvent;
import com.thoughtworks.martdhis2sync.model.MappingJson;
import com.thoughtworks.martdhis2sync.step.ProgramDataSyncStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.SyncFailedException;
import java.util.LinkedList;

@Service
public class ProgramDataSyncService {
    private static final String LOG_PREFIX = "Completed Enrollments: ";
    private static final String JOB_NEW_COMPLETED_ENROLLMENTS = "New Completed Enrollments";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProgramDataSyncStep step;

    @Autowired
    private JobService jobService;

    public void syncProgramDetails(DhisSyncEvent event, MappingJson mappingJson)
            throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException, SyncFailedException {

        LinkedList<Step> steps = new LinkedList<>();
        steps.add(step.get(event.getEncounterId(), mappingJson));
        triggerJob(event.getProgramId(), event.getUserName(), steps, JOB_NEW_COMPLETED_ENROLLMENTS, "");
    }

    private void triggerJob(String service, String user, LinkedList<Step> steps, String jobName, String openLatestCompletedEnrollment)
            throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, SyncFailedException {
        try {
            jobService.triggerJob(service, user, jobName, steps, openLatestCompletedEnrollment);
        } catch (Exception e) {
            logger.error(LOG_PREFIX + e.getMessage());
            throw e;
        }
    }
}
