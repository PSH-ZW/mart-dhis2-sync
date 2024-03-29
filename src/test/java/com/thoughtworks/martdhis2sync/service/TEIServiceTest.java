package com.thoughtworks.martdhis2sync.service;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class TEIServiceTest {

//    @Mock
//    private TrackedEntityInstanceStep instanceStep;
//
//    @Mock
//    private JobService jobService;
//
//    @Mock
//    private Step step;
//
//    @Mock
//    private MappingDAO mappingDAO;
//
//    @Mock
//    private PatientDAO patientDAO;
//
//    @Mock
//    private SyncRepository syncRepository;
//
//    @Mock
//    private ResponseEntity<TrackedEntityInstanceResponse> responseEntity;
//
//    @Mock
//    private TrackedEntityInstanceResponse response;
//
//    public static final String TRACKED_ENTITY_INSTANCE_URI = "/api/trackedEntityInstances?pageSize=10000";
//    private String ORG_UNIT_ID = "DiszpKrYNg8";
//    private int TEI_FILTER_URI_LIMIT = 5;
//    private ResponseEntity<TrackedEntityInstanceResponse> trackedEntityInstanceResponse;
//    private HashMap<String, Object> expectedMapping;
//    private TEIService teiService;
//    private LinkedList<Step> steps = new LinkedList<>();
//
//    @Before
//    public void setUp() throws Exception {
//        teiService = new TEIService();
////        setValuesForMemberFields(teiService, "trackedEntityInstanceStep", instanceStep);
////        setValuesForMemberFields(teiService, "jobService", jobService);
////        setValuesForMemberFields(teiService, "mappingDAO", mappingDAO);
////        setValuesForMemberFields(teiService, "patientDAO", patientDAO);
////        setValuesForMemberFields(teiService, "syncRepository", syncRepository);
////        setValuesForMemberFields(teiService, "orgUnitID", ORG_UNIT_ID);
////        setValuesForMemberFields(teiService, "TEI_FILTER_URI_LIMIT", TEI_FILTER_URI_LIMIT);
//
//        steps.add(step);
//
//        TEIUtil.setInstancesWithEnrollments(new HashMap<>());
//    }
//
//    @Test
//    public void shouldTriggerTheJob() throws Exception {
//        String lookUpTable = "patient_identifier";
//        Object mappingObj = "";
//        String service = "serviceName";
//        String user = "Admin";
//        String jobName = "Sync Tracked Entity Instance";
//        List<String> searchableAttributes = Arrays.asList("UIC", "date_created");
//        List<String> comparableAttributes = Arrays.asList("patient_id", "prepID");
//
//        doNothing().when(jobService).triggerJob(service, user, jobName, steps, "");
//        when(instanceStep.get(lookUpTable, service, mappingObj, searchableAttributes, comparableAttributes)).thenReturn(step);
//
////        teiService.triggerJob(service, user, lookUpTable, mappingObj, searchableAttributes, comparableAttributes);
//
//        verify(jobService, times(1)).triggerJob(service, user, jobName, steps, "");
//    }
//
//    @Test(expected = JobExecutionAlreadyRunningException.class)
//    public void shouldThrowJobExecutionAlreadyRunningException() throws Exception {
//        String lookUpTable = "patient_identifier";
//        Object mappingObj = "";
//        String service = "serviceName";
//        String user = "Admin";
//        String jobName = "Sync Tracked Entity Instance";
//        List<String> searchableAttributes = Arrays.asList("UIC", "date_created");
//        List<String> comparableAttributes = Arrays.asList("patient_id", "prepID");
//
//        when(instanceStep.get(lookUpTable, service, mappingObj, searchableAttributes, comparableAttributes)).thenReturn(step);
//        doThrow(JobExecutionAlreadyRunningException.class).when(jobService)
//                .triggerJob(service, user, jobName, steps, "");
//
//        try {
////            teiService.triggerJob(service, user, lookUpTable, mappingObj, searchableAttributes, comparableAttributes);
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//
//    @Test(expected = SyncFailedException.class)
//    public void shouldThrowSyncFailedException() throws Exception {
//        String lookUpTable = "patient_identifier";
//        Object mappingObj = "";
//        String service = "serviceName";
//        String user = "Admin";
//        String jobName = "Sync Tracked Entity Instance";
//        List<String> searchableAttributes = Arrays.asList("UIC", "date_created");
//        List<String> comparableAttributes = Arrays.asList("patient_id", "prepID");
//
//        when(instanceStep.get(lookUpTable, service, mappingObj, searchableAttributes, comparableAttributes)).thenReturn(step);
//        doThrow(SyncFailedException.class).when(jobService)
//                .triggerJob(service, user, jobName, steps, "");
//
//        try {
////            teiService.triggerJob(service, user, lookUpTable, mappingObj, searchableAttributes, comparableAttributes);
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//
//    @Test
//    public void shouldHaveEmptyListForInstanceWithEnrollments() throws Exception {
//        String enrollment = "enrollmentTable";
//        String programName = "HTS";
//        String eventTable = "eventTable";
////        when(patientDAO.getDeltaEnrollmentInstanceIds(enrollment, eventTable, programName)).thenReturn(new ArrayList<>());
//
////        teiService.getEnrollmentsForInstances(enrollment, eventTable, programName);
//
//        assertEquals(0, TEIUtil.getInstancesWithEnrollments().size());
//    }
//
//    @Test
//    public void shouldReturnEnrollmentsForTheGivenProgramAndGivenInstances() throws Exception {
//        String enrollment = "enrollmentTable";
//        String programName = "HTS";
//        String eventTable = "eventTable";
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("instance_id", "instance1");
//        map1.put("program", "program");
//
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("instance_id", "instance2");
//        map2.put("program", "program");
//
//        String url = "/api/trackedEntityInstances?" +
//                "fields=trackedEntityInstance,enrollments[program,enrollment,enrollmentDate,completedDate,status]&" +
//                "program=program&trackedEntityInstance=instance1;instance2";
//
//        EnrollmentDetails enrollment1 = new EnrollmentDetails("program", "enrollment1", "2018-10-22", "2018-12-10", "COMPLETED");
//        EnrollmentDetails enrollment2 = new EnrollmentDetails("program", "enrollment2", "2018-10-22", null, "ACTIVE");
//        EnrollmentDetails enrollment3 = new EnrollmentDetails("program2", "enrollment3", "2018-10-22", null, "ACTIVE");
//        EnrollmentDetails enrollment4 = new EnrollmentDetails("program2", "enrollment4", "2018-10-22", null, "ACTIVE");
//
//        TrackedEntityInstanceInfo trackedEntityInstance1 = new TrackedEntityInstanceInfo();
//        trackedEntityInstance1.setEnrollments(Arrays.asList(enrollment1, enrollment2, enrollment3, enrollment4));
//        trackedEntityInstance1.setTrackedEntityInstance("instance1");
//        TrackedEntityInstanceInfo trackedEntityInstance2 = new TrackedEntityInstanceInfo();
//        trackedEntityInstance2.setTrackedEntityInstance("instance2");
//        trackedEntityInstance2.setEnrollments(Collections.emptyList());
//
////        when(patientDAO.getDeltaEnrollmentInstanceIds(enrollment, eventTable, programName)).thenReturn(Arrays.asList(map1, map2));
//        when(syncRepository.getTrackedEntityInstances(url)).thenReturn(responseEntity);
//        when(responseEntity.getBody()).thenReturn(response);
//        when(response.getTrackedEntityInstances()).thenReturn(Arrays.asList(trackedEntityInstance1, trackedEntityInstance2));
//
////        teiService.getEnrollmentsForInstances(enrollment, eventTable, programName);
//
//        Map<String, List<EnrollmentDetails>> expected = new HashMap<>();
//        expected.put("instance1", Arrays.asList(enrollment1, enrollment2));
//
//        assertEquals(expected, TEIUtil.getInstancesWithEnrollments());
//    }
//
//    @Test
//    public void shouldReturnAddAnyEnrollmentsIfSyncRepoThrowsError() throws Exception {
//        String enrollment = "enrollmentTable";
//        String programName = "HTS";
//        String eventTable = "eventTable";
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("instance_id", "instance1");
//        map1.put("program", "program");
//
//        Map<String, Object> map2 = new HashMap<>();
//        map2.put("instance_id", "instance2");
//        map2.put("program", "program");
//
//        String url = "/api/trackedEntityInstances?" +
//                "fields=trackedEntityInstance,enrollments[program,enrollment,enrollmentDate,completedDate,status]&" +
//                "program=program&trackedEntityInstance=instance1;instance2";
//
////        when(patientDAO.getDeltaEnrollmentInstanceIds(enrollment, eventTable, programName)).thenReturn(Arrays.asList(map1, map2));
//        when(syncRepository.getTrackedEntityInstances(url)).thenThrow(new HttpServerErrorException(HttpStatus.CONFLICT));
//
//        try {
////            teiService.getEnrollmentsForInstances(enrollment, eventTable, programName);
//        } catch (Exception e) {
//            assertEquals(0, TEIUtil.getInstancesWithEnrollments().size());
//        }
//    }
//
//    @Test
//    public void shouldGetTrackedEntityInstanceFromDHIS() throws IOException {
//        String program = "HIV Testing Service";
//        String queryParams = "&filter=HF8Tu4tg:IN:NINETU190995MT;JKAPTA170994MT;";
//        String uri = TRACKED_ENTITY_INSTANCE_URI + "&ou=" + ORG_UNIT_ID + "&ouMode=DESCENDANTS" + queryParams;
//        Map<String, Object> searchableMapping = new HashMap<>();
//
//        trackedEntityInstanceResponse = ResponseEntity.ok(new TrackedEntityInstanceResponse(getTrackedEntityInstances(), "", 200));
//
//        MappingJson mappingJson = new MappingJson();
////        mappingJson.setInstance("{" +
////                "\"UIC\": \"HF8Tu4tg\"," +
////                "\"date_created\": \"ojmUIu4tg\"" +
////                "}");
//        searchableMapping.put("UIC", "HF8Tu4tg");
//
//        when(mappingDAO.getSearchableFields(program)).thenReturn(getSearchableValues());
//        when(mappingDAO.getMapping(program)).thenReturn(expectedMapping);
//        when(syncRepository.getTrackedEntityInstances(uri)).thenReturn(trackedEntityInstanceResponse);
//
////        teiService.getTrackedEntityInstances(program, mappingJson);
//
//        verify(mappingDAO, times(1)).getSearchableFields(program);
//        verify(syncRepository, times(1)).getTrackedEntityInstances(uri);
//        verifyStatic(times(1));
//        TEIUtil.setTrackedEntityInstanceInfos(getTrackedEntityInstances());
//    }
//
//    @Test
//    public void shouldNotGetTrackedEntityInstanceIfSearchblesIsEmpty() throws IOException {
//        String program = "HIV Testing Service";
//
//        MappingJson mappingJson = new MappingJson();
////        mappingJson.setInstance("{" +
////                "\"UIC\": \"HF8Tu4tg\"," +
////                "\"date_created\": \"ojmUIu4tg\"" +
////                "}");
//
//        when(mappingDAO.getSearchableFields(program)).thenReturn(new ArrayList<>());
//
////        teiService.getTrackedEntityInstances(program, mappingJson);
//
//        verify(mappingDAO, times(1)).getSearchableFields(program);
//        verifyStatic(times(1));
//        TEIUtil.setTrackedEntityInstanceInfos(Collections.emptyList());
//
//        assertEquals(0, TEIUtil.getTrackedEntityInstanceInfos().size());
//    }
//
//    @Test
//    public void shouldSearchForFiveHundredTEIsAtATimeAndMakeTheCallMultipleTimesAccordingly() throws IOException {
//        String program = "HIV Testing Service";
//        String firstFiveUICs = "I14NM8TQ8A;K195G5NR3Q;XHPW96RN9H;5M7SEGZ1Q3;CR4XHTG1O7;";
//        String lastFourUICs = "E8WXHJPWUG;UIBXG5IM3O;ZQT65ZLHRL;54KE7VC6AL;";
//        String queryParams = "&filter=HF8Tu4tg:IN:";
//        String uriWithoutSearchValues = TRACKED_ENTITY_INSTANCE_URI + "&ou=" + ORG_UNIT_ID + "&ouMode=DESCENDANTS";
//
//        trackedEntityInstanceResponse = ResponseEntity.ok(new TrackedEntityInstanceResponse(getTrackedEntityInstances(), "", 200));
//
//        MappingJson mappingJson = new MappingJson();
////        mappingJson.setInstance("{" +
////                "\"UIC\": \"HF8Tu4tg\"," +
////                "\"date_created\": \"ojmUIu4tg\"" +
////                "}");
//
//        when(mappingDAO.getSearchableFields(program)).thenReturn(getTenSearchableValues());
//        when(mappingDAO.getMapping(program)).thenReturn(expectedMapping);
//        when(syncRepository.getTrackedEntityInstances(anyString())).thenReturn(trackedEntityInstanceResponse);
//
////        teiService.getTrackedEntityInstances(program, mappingJson);
//
//        verify(mappingDAO, times(1)).getSearchableFields(program);
//        verify(syncRepository, times(1)).getTrackedEntityInstances(uriWithoutSearchValues + queryParams + firstFiveUICs);
//        verify(syncRepository, times(1)).getTrackedEntityInstances(uriWithoutSearchValues + queryParams + lastFourUICs);
//        verifyStatic(times(1));
//        TEIUtil.setTrackedEntityInstanceInfos(getTrackedEntityInstances());
//    }
//
//    private List<TrackedEntityInstanceInfo> getTrackedEntityInstances() {
//        List<TrackedEntityInstanceInfo> trackedEntityInstanceInfos = new LinkedList<>();
//        List<Attribute> attributesOfPatient1 = new ArrayList<>();
//        List<Attribute> attributesOfPatient2 = new ArrayList<>();
//
//        attributesOfPatient1.add(new Attribute(
//                "2018-11-26T09:24:57.158",
//                "admin",
//                "MMD_PER_NAM",
//                "First name",
//                "2018-11-26T09:24:57.158",
//                "TEXT",
//                "w75KJ2mc4zz",
//                "Michel"
//        ));
//
//        attributesOfPatient1.add(new Attribute(
//                "2018-11-26T09:24:57.153",
//                "admin",
//                "",
//                "Last name",
//                "2018-11-26T09:24:57.152",
//                "TEXT",
//                "zDhUuAYrxNC",
//                "Jackson"
//        ));
//
//        trackedEntityInstanceInfos.add(new TrackedEntityInstanceInfo(
//                "2018-09-21T17:54:00.294",
//                "SxgCPPeiq3c",
//                "2018-09-21T17:54:01.337",
//                "w3MoRtzP4SO",
//                "2018-09-21T17:54:01.337",
//                "o0kaqrZa79Y",
//                "2018-09-21T17:54:01.337",
//                false,
//                false,
//                "NONE",
//                Collections.emptyList(),
//                Collections.emptyList(),
//                Collections.emptyList(),
//                attributesOfPatient1
//        ));
//
//        attributesOfPatient2.add(new Attribute(
//                "2018-11-26T09:24:57.158",
//                "admin",
//                "MMD_PER_NAM",
//                "First name",
//                "2018-11-26T09:24:57.158",
//                "TEXT",
//                "w75KJ2mc4zz",
//                "Jinny"
//        ));
//
//        attributesOfPatient2.add(new Attribute(
//                "2018-11-26T09:24:57.153",
//                "admin",
//                "",
//                "Last name",
//                "2018-11-26T09:24:57.152",
//                "TEXT",
//                "zDhUuAYrxNC",
//                "Jackson"
//        ));
//
//        trackedEntityInstanceInfos.add(new TrackedEntityInstanceInfo(
//                "2018-09-22T13:24:00.24",
//                "SxgCPPeiq3c",
//                "2018-09-21T17:54:01.337",
//                "tzP4SOw3MoR",
//                "2018-09-22T13:24:00.241",
//                "o0kaqrZa79Y",
//                "2018-09-21T17:54:01.337",
//                false,
//                false,
//                "NONE",
//                Collections.emptyList(),
//                Collections.emptyList(),
//                Collections.emptyList(),
//                attributesOfPatient2
//        ));
//
//        return trackedEntityInstanceInfos;
//    }
//
//    private List<Map<String, Object>> getSearchableValues() {
//        List<Map<String, Object>> searchableValues = new LinkedList<>();
//
//        Map<String, Object> searchable1 = new HashMap<>();
//        searchable1.put("UIC", "NINETU190995MT");
//        searchableValues.add(searchable1);
//
//        Map<String, Object> searchable2 = new HashMap<>();
//        searchable2.put("UIC", "JKAPTA170994MT");
//        searchableValues.add(searchable2);
//
//        return searchableValues;
//    }
//
//    private List<Map<String, Object>> getTenSearchableValues() {
//        List<Map<String, Object>> searchableValues = new LinkedList<>();
//
//        Map<String, Object> searchable1 = new HashMap<>();
//        searchable1.put("UIC", "I14NM8TQ8A");
//        searchableValues.add(searchable1);
//
//        Map<String, Object> searchable2 = new HashMap<>();
//        searchable2.put("UIC", "K195G5NR3Q");
//        searchableValues.add(searchable2);
//
//        Map<String, Object> searchable3 = new HashMap<>();
//        searchable3.put("UIC", "XHPW96RN9H");
//        searchableValues.add(searchable3);
//
//        Map<String, Object> searchable4 = new HashMap<>();
//        searchable4.put("UIC", "5M7SEGZ1Q3");
//        searchableValues.add(searchable4);
//
//        Map<String, Object> searchable5 = new HashMap<>();
//        searchable5.put("UIC", "CR4XHTG1O7");
//        searchableValues.add(searchable5);
//
//        Map<String, Object> searchable6 = new HashMap<>();
//        searchable6.put("UIC", "E8WXHJPWUG");
//        searchableValues.add(searchable6);
//
//        Map<String, Object> searchable7 = new HashMap<>();
//        searchable7.put("UIC", "UIBXG5IM3O");
//        searchableValues.add(searchable7);
//
//        Map<String, Object> searchable8 = new HashMap<>();
//        searchable8.put("UIC", "ZQT65ZLHRL");
//        searchableValues.add(searchable8);
//
//        Map<String, Object> searchable9 = new HashMap<>();
//        searchable9.put("UIC", "54KE7VC6AL");
//        searchableValues.add(searchable9);
//
//        return searchableValues;
//    }

}
