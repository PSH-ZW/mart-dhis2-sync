package com.thoughtworks.martdhis2sync.step;

import com.thoughtworks.martdhis2sync.model.MappingJson;
import com.thoughtworks.martdhis2sync.processor.NewEnrollmentWithEventsProcessor;
import com.thoughtworks.martdhis2sync.reader.MappingReader;
import com.thoughtworks.martdhis2sync.util.BatchUtil;
import com.thoughtworks.martdhis2sync.util.Constants;
import com.thoughtworks.martdhis2sync.writer.NewActiveAndCompletedEnrollmentWithEventsWriter;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProgramDataSyncStep {

    @Autowired
    private MappingReader mappingReader;

    @Autowired
    private ObjectFactory<NewEnrollmentWithEventsProcessor> processorObjectFactory;

    @Autowired
    private NewActiveAndCompletedEnrollmentWithEventsWriter writer;

    @Autowired
    private StepFactory stepFactory;

    private static final String STEP_NAME = "Enrollment and Event data sync";

    public Step get(String encounterId, MappingJson mappingObj) {

        return stepFactory.build(STEP_NAME,
                mappingReader.getEnrollmentAndEventReader(encounterId, mappingObj),
                getProcessor(mappingObj),
                writer);
    }

    private NewEnrollmentWithEventsProcessor getProcessor(MappingJson mappingObj) {
        NewEnrollmentWithEventsProcessor processor = processorObjectFactory.getObject();
        Map<String, String> columnMappingsWithProgramStageId = new HashMap<>();
        Map<String, Map<String, Map<String, String>>> formTableMappings = mappingObj.getFormTableMappings();
        for(String table : formTableMappings.keySet()) {
            Map<String, Map<String, String>> columnMappingWithElementNameAndId = formTableMappings.get(table);
            Map<String, String> columnMappingWithId = BatchUtil.getColumnNameToDhisElementIdMap(table, columnMappingWithElementNameAndId);
            columnMappingsWithProgramStageId.putAll(columnMappingWithId);
        }
        columnMappingsWithProgramStageId.put(Constants.DHIS_PROGRAM_STAGE_ID, mappingObj.getDhisProgramStageId().getId());
        processor.setMappingObj(columnMappingsWithProgramStageId);

        return processor;
    }


}
