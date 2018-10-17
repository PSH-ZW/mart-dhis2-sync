package com.thoughtworks.martdhis2sync.writer;

import com.thoughtworks.martdhis2sync.model.*;
import com.thoughtworks.martdhis2sync.repository.SyncRepository;
import com.thoughtworks.martdhis2sync.service.LoggerService;
import com.thoughtworks.martdhis2sync.util.EnrollmentUtil;
import com.thoughtworks.martdhis2sync.util.MarkerUtil;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.martdhis2sync.CommonTestHelper.setValuesForMemberFields;
import static com.thoughtworks.martdhis2sync.model.Conflict.CONFLICT_OBJ_ENROLLMENT_DATE;
import static com.thoughtworks.martdhis2sync.model.Conflict.CONFLICT_OBJ_ENROLLMENT_INCIDENT_DATE;
import static com.thoughtworks.martdhis2sync.model.ImportSummary.IMPORT_SUMMARY_RESPONSE_ERROR;
import static com.thoughtworks.martdhis2sync.model.ImportSummary.IMPORT_SUMMARY_RESPONSE_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EnrollmentUtil.class})
@PowerMockIgnore("javax.management.*")
public class ProgramEnrollmentWriterTest {
    @Mock
    private SyncRepository syncRepository;

    @Mock
    private DataSource dataSource;

    @Mock
    private DHISSyncResponse DHISSyncResponse;

    @Mock
    private ResponseEntity<DHISSyncResponse> responseEntity;

    @Mock
    private Response response;

    @Mock
    private List<ImportSummary> importSummaries;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private MarkerUtil markerUtil;

    @Mock
    private LoggerService loggerService;

    private ProgramEnrollmentWriter writer;

    private static final String uri = "/api/enrollments?strategy=CREATE_AND_UPDATE";

    private static final String EMPTY_STRING = "\"\"";

    private static List<String> referenceUIDs = Arrays.asList("enrollmentABC", "enrollmentXYZ");

    private static List<String> instanceIDs = Arrays.asList("instanceABC", "instanceXYZ");

    private String requestBody;

    private List<? extends Enrollment> list;

    private String programName = "HTS Service";

    private static final String ENROLLMENT_API_FORMAT = "{\"enrollment\": %s, " +
            "\"trackedEntityInstance\": \"%s\", " +
            "\"orgUnit\":\"%s\"," +
            "\"program\":\"%s\"," +
            "\"enrollmentDate\":\"%s\"," +
            "\"incidentDate\":\"%s\"," +
            "\"status\": \"%s\"}";

    @Before
    public void setUp() throws Exception {
        writer = new ProgramEnrollmentWriter();

        setValuesForMemberFields(writer, "syncRepository", syncRepository);
        setValuesForMemberFields(writer, "dataSource", dataSource);
        setValuesForMemberFields(writer, "markerUtil", markerUtil);
        setValuesForMemberFields(writer, "responseEntity", responseEntity);
        setValuesForMemberFields(writer, "loggerService", loggerService);

        Enrollment enrollments1 = new Enrollment(EMPTY_STRING, "tm02QkL2wJP", "aHoRX5uGMLU",
                "ORG_UNIT", "2018-09-14", "2018-09-14", "ACTIVE", "1");
        Enrollment enrollments2 = new Enrollment(EMPTY_STRING, "tm02QkL2wJP", "aHoRX5uGMLU",
                "ORG_UNIT", "2018-09-14", "2018-09-14", "ACTIVE", "1");

        list = Arrays.asList(enrollments1, enrollments2);
        requestBody = getRequestBody(list);

        mockStatic(EnrollmentUtil.class);
    }

    private String getRequestBody(List<? extends Enrollment> list) {
        StringBuilder body = new StringBuilder("{\"enrollments\":[");
        list.forEach(enrollment -> {
            String enr = String.format(
                    ENROLLMENT_API_FORMAT,
                    enrollment.getEnrollment_id(), enrollment.getInstance_id(),
                    enrollment.getOrgUnit(), enrollment.getProgram(),
                    enrollment.getProgram_start_date(), enrollment.getIncident_date(),
                    enrollment.getStatus().toUpperCase());
            body.append(enr).append(",");
        });
        body.replace(body.length() - 1, body.length(), "]}");
        return body.toString();
    }


    @Test
    @SneakyThrows
    public void shouldCallSyncRepoToSendData() {
        when(syncRepository.sendData(uri, requestBody)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(DHISSyncResponse);
        when(DHISSyncResponse.getResponse()).thenReturn(response);
        when(response.getImportSummaries()).thenReturn(new ArrayList<>());

        writer.write(list);

        verify(syncRepository, times(1)).sendData(uri, requestBody);
    }

    @Test
    @SneakyThrows
    public void shouldNotUpdateTrackerTableAfterSendingOnlyUpdatedEnrollmentsInSync() {

        importSummaries = Arrays.asList(
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(0, 1, 0, 0), null, new ArrayList<>(), referenceUIDs.get(0)),
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(0, 1, 0, 0), null, new ArrayList<>(), referenceUIDs.get(1)));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(DHISSyncResponse);
        when(DHISSyncResponse.getResponse()).thenReturn(response);
        when(response.getImportSummaries()).thenReturn(importSummaries);
        when(syncRepository.sendData(uri, requestBody)).thenReturn(responseEntity);

        writer.write(list);

        verify(syncRepository, times(1)).sendData(uri, requestBody);
        verify(dataSource, times(0)).getConnection();
    }

    @Test
    @SneakyThrows
    public void shouldUpdateTrackerAndMarkerTablesOnSuccessfullySyncingOnlyNewEnrollments() {

        importSummaries = Arrays.asList(
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(1, 0, 0, 0), null, new ArrayList<>(), referenceUIDs.get(0)),
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(1, 0, 0, 0), null, new ArrayList<>(), referenceUIDs.get(1)));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(DHISSyncResponse);
        when(DHISSyncResponse.getResponse()).thenReturn(response);
        when(response.getImportSummaries()).thenReturn(importSummaries);
        when(syncRepository.sendData(uri, requestBody)).thenReturn(responseEntity);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        writer.write(list);

        verify(syncRepository, times(1)).sendData(uri, requestBody);
        verify(dataSource, times(1)).getConnection();
        verify(preparedStatement, times(2)).executeUpdate();
        verify(markerUtil, times(1)).updateMarkerEntry(anyString(), anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldHandleSQLException() {

        importSummaries = Arrays.asList(
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(1, 0, 0, 0), null, new ArrayList<>(), referenceUIDs.get(0)),
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(1, 0, 0, 0), null, new ArrayList<>(), referenceUIDs.get(1)));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(DHISSyncResponse);
        when(DHISSyncResponse.getResponse()).thenReturn(response);
        when(response.getImportSummaries()).thenReturn(importSummaries);
        when(syncRepository.sendData(uri, requestBody)).thenReturn(responseEntity);

        when(dataSource.getConnection()).thenThrow(new SQLException());

        try {
            writer.write(list);
        } catch (Exception e) {
            assertEquals(SQLException.class, e.getClass());
        }

        verify(syncRepository, times(1)).sendData(uri, requestBody);
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    @SneakyThrows
    public void shouldNotUpdateMarkerWhenSyncingEnrollmentWithIncorrectProgramUIDAndSyncFailsWith500InternalServerError() {
        when(syncRepository.sendData(uri, requestBody)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            writer.write(list);
        } catch (Exception e) {
            assertEquals(HttpServerErrorException.class, e.getClass());
        }

        verify(syncRepository, times(1)).sendData(uri, requestBody);
        verify(dataSource, times(0)).getConnection();
        verify(markerUtil, times(0)).updateMarkerEntry(anyString(), anyString(), anyString());
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionWhenEnrollmentSyncFails() throws Exception {
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.CONFLICT);

        writer.write(list);
    }

    @Test
    @SneakyThrows
    public void shouldLogConflictsAndThrowExceptionOnEnrollmentSyncFailureWith409Conflict() {
        List<Conflict> conflicts = Arrays.asList(
                new Conflict(CONFLICT_OBJ_ENROLLMENT_INCIDENT_DATE, "Incident Date can't be future date :Mon Oct 01 00:00:00 IST 2018"),
                new Conflict(CONFLICT_OBJ_ENROLLMENT_DATE, "Enrollment Date can't be future date :Mon Sept 01 00:00:00 IST 2018"));
        importSummaries = Arrays.asList(
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_ERROR,
                        new ImportCount(0, 0, 1, 0), null, conflicts, referenceUIDs.get(0)),
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_ERROR,
                        new ImportCount(0, 0, 1, 0), null, conflicts, referenceUIDs.get(1)));

        when(syncRepository.sendData(uri, requestBody)).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(DHISSyncResponse);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(DHISSyncResponse.getResponse()).thenReturn(response);
        when(response.getImportSummaries()).thenReturn(importSummaries);

        try {
            writer.write(list);
        } catch (Exception e) {
            assertEquals(e.getClass(), Exception.class);
        }

        verify(syncRepository, times(1)).sendData(uri, requestBody);
        verify(dataSource, times(0)).getConnection();
        verify(markerUtil, times(0)).updateMarkerEntry(anyString(), anyString(), anyString());
        verify(loggerService, times(4)).collateLogInfo(anyString());
    }

    @Test
    @SneakyThrows
    public void shouldLogDescriptionAndThrowExceptionOnEnrollmentSyncFailureWith409Conflict() {
        String expected = "TrackedEntityInstance TEI_UID_1 already has an active enrollment in program Ox4qJuR5jAI";
        ImportSummary importSummaryForPatientWithAlreadyActiveEnrollment = new ImportSummary("", IMPORT_SUMMARY_RESPONSE_ERROR,
                new ImportCount(0, 0, 1, 0),
                "TrackedEntityInstance TEI_UID_1 already has an active enrollment in program Ox4qJuR5jAI", new ArrayList<>(), null);
        ImportSummary importSummaryForNewPatientWithoutAnyEnrollments = new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                new ImportCount(1, 0, 0, 0), null, new ArrayList<>(), referenceUIDs.get(1));

        importSummaries = Arrays.asList(importSummaryForPatientWithAlreadyActiveEnrollment, importSummaryForNewPatientWithoutAnyEnrollments);

        when(syncRepository.sendData(uri, requestBody)).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(DHISSyncResponse);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(DHISSyncResponse.getResponse()).thenReturn(response);
        when(response.getImportSummaries()).thenReturn(importSummaries);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        try {
            writer.write(list);
        } catch (Exception e) {
            assertEquals(e.getClass(), Exception.class);
        }

        verify(syncRepository, times(1)).sendData(uri, requestBody);
        verify(dataSource, times(1)).getConnection();
        verify(preparedStatement, times(1)).executeUpdate();
        verify(markerUtil, times(0)).updateMarkerEntry(anyString(), anyString(), anyString());
        verify(loggerService, times(1)).collateLogInfo(expected);
    }

    @Test
    @SneakyThrows
    public void shouldUpdateTrackerAndMarkerTablesOnSuccessfullySyncingOnlyCompletedOrCancelledEnrollments() {

        Enrollment enrollments1 = new Enrollment(referenceUIDs.get(0), "tm02QkL2wJP", "aHoRX5uGMLU",
                "ORG_UNIT", "2018-09-14", "2018-09-14", "COMPLETED", "1");
        Enrollment enrollments2 = new Enrollment(referenceUIDs.get(1), "tm02QkL2wJP", "aHoRX5uGMLU",
                "ORG_UNIT", "2018-09-14", "2018-09-14", "CANCELLED", "1");

        list = Arrays.asList(enrollments1, enrollments2);
        requestBody = getRequestBody(list);

        importSummaries = Arrays.asList(
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(0, 1, 0, 0), null, new ArrayList<>(), referenceUIDs.get(0)),
                new ImportSummary("", IMPORT_SUMMARY_RESPONSE_SUCCESS,
                        new ImportCount(0, 1, 0, 0), null, new ArrayList<>(), referenceUIDs.get(1)));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(DHISSyncResponse);
        when(DHISSyncResponse.getResponse()).thenReturn(response);
        when(response.getImportSummaries()).thenReturn(importSummaries);
        when(syncRepository.sendData(uri, requestBody)).thenReturn(responseEntity);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        writer.write(list);

        verify(syncRepository, times(1)).sendData(uri, requestBody);
        verify(dataSource, times(1)).getConnection();
        verify(preparedStatement, times(2)).executeUpdate();
        verify(markerUtil, times(1)).updateMarkerEntry(anyString(), anyString(), anyString());
    }
}
