package com.thoughtworks.martdhis2sync.service;

import com.thoughtworks.martdhis2sync.dao.EnrollmentDAO;
import com.thoughtworks.martdhis2sync.dao.PatientDAO;
import com.thoughtworks.martdhis2sync.model.EnrollmentAPIPayLoad;
import com.thoughtworks.martdhis2sync.model.Enrollment;
import com.thoughtworks.martdhis2sync.repository.SyncRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EnrollmentService {

    @Value("${dhis2.program-id}")
    private String programId;

    @Value("${country.org.unit.id.for.patient.data.duplication.check}")
    private String parentOrgUnit;

    @Autowired
    EnrollmentDAO enrollmentDAO;

    @Autowired
    PatientDAO patientDAO;

    @Autowired
    SyncRepository syncRepository;

    public boolean enrollmentExistsInTracker(String patientId) {
        String dhisInstanceId = patientDAO.getInstanceIdForPatient(patientId);
        return enrollmentDAO.enrolmentExistsInEnrolmentTracker(dhisInstanceId);
    }

    public void addEnrollmentToTrackerIfEnrollmentExistsInDhis(String patientId) {
        String dhisInstanceId = patientDAO.getInstanceIdForPatient(patientId);
        String enrollmentId = getEnrollmentFromDhis(dhisInstanceId);
        if(StringUtils.hasLength(enrollmentId)) {
            EnrollmentAPIPayLoad enrollment = new EnrollmentAPIPayLoad();
            enrollment.setInstanceId(dhisInstanceId);
            enrollment.setEnrollmentId(enrollmentId);
            enrollmentDAO.insertIntoEnrollmentTracker(enrollment);
        }
    }

    private String getEnrollmentFromDhis(String dhisInstanceId) {
        String URI = "/api/enrollments?trackedEntityInstance=%s&program=%s&ou=%s&ouMode=DESCENDANTS&programStatus=ACTIVE";
        URI = String.format(URI, dhisInstanceId, programId, parentOrgUnit);
        Enrollment enrollment = syncRepository.getEnrollment(URI);
        if(enrollment != null && StringUtils.hasLength(enrollment.getEnrollment())) {
            return enrollment.getEnrollment();
        }

        return "";
    }
}
