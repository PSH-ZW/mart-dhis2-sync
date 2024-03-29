package com.thoughtworks.martdhis2sync.repository;

import com.google.gson.Gson;
import com.thoughtworks.martdhis2sync.model.*;
import com.thoughtworks.martdhis2sync.service.LoggerService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.List;

@Repository
public class SyncRepository {

    @Value("${dhis2.url}")
    private String dhis2Url;

    @Value("${dhis2.user}")
    private String dhisUser;

    @Value("${dhis2.password}")
    private String dhisPassword;

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String LOG_PREFIX = "SyncRepository: ";

    public ResponseEntity<DHISSyncResponse> sendData(String uri, String body) {
        return sync(uri, body, DHISSyncResponse.class);
    }

    public ResponseEntity<DHISEnrollmentSyncResponse> sendEnrollmentData(String uri, String body) {
        return sync(uri, body, DHISEnrollmentSyncResponse.class);
    }

    public ResponseEntity<DataElementResponse> getDataElements(String url) {
        ResponseEntity<DataElementResponse> responseEntity = null;
        try {
            responseEntity = restTemplate
                    .exchange(url, HttpMethod.GET,
                            new HttpEntity<>(getHttpHeaders()), DataElementResponse.class);
            logger.info(LOG_PREFIX + "Received " + responseEntity.getStatusCode() + " status code.");

        }catch (Exception e){
            logger.error(LOG_PREFIX + e);
        }
        return responseEntity;
    }

    public ResponseEntity<TrackedEntityAttributeResponse> getTrackedEntityAttributes(String url) {
        ResponseEntity<TrackedEntityAttributeResponse> responseEntity = null;
        try {
            responseEntity = restTemplate
                    .exchange(url, HttpMethod.GET,
                            new HttpEntity<>(getHttpHeaders()), TrackedEntityAttributeResponse.class);
            logger.info(LOG_PREFIX + "Received " + responseEntity.getStatusCode() + " status code.");

        }catch (Exception e){
            logger.error(LOG_PREFIX + e);
        }
        return responseEntity;
    }

    public ResponseEntity<TrackedEntityInstanceResponse> getTrackedEntityInstances(String uri) {
        ResponseEntity<TrackedEntityInstanceResponse> responseEntity;
        try {
            logger.info("Tracked Entity Request URI---> "+ uri);

            responseEntity = restTemplate
                    .exchange(dhis2Url + uri, HttpMethod.GET,
                            new HttpEntity<>(getHttpHeaders()), TrackedEntityInstanceResponse.class);

           logger.info("Response---------->\n" + responseEntity);

            logger.info(LOG_PREFIX + "Received " + responseEntity.getStatusCode() + " status code.");

        } catch (HttpClientErrorException e) {
            responseEntity = new ResponseEntity<>(
                    new Gson().fromJson(e.getResponseBodyAsString(), TrackedEntityInstanceResponse.class),
                    e.getStatusCode());
            TrackedEntityInstanceResponse body = responseEntity.getBody();
            logger.error("HttpClientErrorException -> " + responseEntity.getBody());
            loggerService.collateLogMessage(String.format("%s %s", body.getHttpStatusCode(), body.getMessage()));
            logger.error(LOG_PREFIX + e);
            throw e;
        } catch (HttpServerErrorException e) {
            loggerService.collateLogMessage(String.format("%s %s", e.getStatusCode(), e.getStatusCode().getReasonPhrase()));
            logger.error(LOG_PREFIX + e);
            throw e;
        }
        return responseEntity;
    }

    private HttpHeaders getHttpHeaders() {
        String auth = dhisUser + ":" + dhisPassword;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", authHeader);

        return httpHeaders;
    }

    private <T> ResponseEntity<T> sync(String uri, String body, Class<T> type) {
        ResponseEntity<T> responseEntity;
        try {

            logger.info("Request URI---> "+ uri);
            logger.info("Request body--->\n"+ body);

            body = body.replace("\n", " ");
            responseEntity = restTemplate
                    .exchange(dhis2Url + uri, HttpMethod.POST, new HttpEntity<>(body, getHttpHeaders()), type);

            logger.info("Response---------->\n" + responseEntity);
            logger.info(LOG_PREFIX + "Received " + responseEntity.getStatusCode() + " status code.");
        } catch (HttpClientErrorException e) {
            responseEntity = new ResponseEntity<>(
                    new Gson().fromJson(e.getResponseBodyAsString(), type),
                    e.getStatusCode());
            logger.error("e.getResponseBodyAsString() -> " + e.getResponseBodyAsString());
            loggerService.collateLogMessage(String.format("%s %s", e.getStatusCode(), e.getStatusCode().getReasonPhrase()));
            logger.error("HttpClientErrorException -> " + responseEntity.getBody());
            logger.error(LOG_PREFIX + e);
        } catch (HttpServerErrorException e) {
            loggerService.collateLogMessage(String.format("%s %s", e.getStatusCode(), e.getStatusCode().getReasonPhrase()));
            logger.error(LOG_PREFIX + e);
            throw e;
        }
        return responseEntity;
    }

    public Enrollment getEnrollment(String uri) {
        ResponseEntity<EnrollmentDetails> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(dhis2Url + uri, HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), EnrollmentDetails.class);
            if(responseEntity != null) {
                EnrollmentDetails body = responseEntity.getBody();
                List<Enrollment> enrollments = body.getEnrollments();
                return enrollments.get(0);
            }

        }catch (Exception e){
            logger.error(LOG_PREFIX + e);
        }
        return  null;
    }
}
