package com.thoughtworks.martdhis2sync.trackerHandler;

import com.thoughtworks.martdhis2sync.dao.EnrollmentDAO;
import com.thoughtworks.martdhis2sync.dao.EventDAO;
import com.thoughtworks.martdhis2sync.model.EnrollmentAPIPayLoad;
import com.thoughtworks.martdhis2sync.model.EventTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackersHandler {

    @Autowired
    private EnrollmentDAO enrollmentDAO;

    @Autowired
    private EventDAO eventDAO;

    public void addEnrollmentForPatient(EnrollmentAPIPayLoad enrollment) {
        if(!enrollmentDAO.enrolmentExistsInEnrolmentTracker(enrollment.getInstanceId())) {
            enrollmentDAO.insertIntoEnrollmentTracker(enrollment);
        }
    }

    public void addEventIdForEncounterAndProgramStage(EventTracker eventTracker) {
        if(!eventDAO.eventExistsInEventTracker(eventTracker.getEventId())) {
            eventDAO.insertIntoEventTracker(eventTracker);
        }
    }
}
