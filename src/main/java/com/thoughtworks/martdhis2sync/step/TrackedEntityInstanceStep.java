package com.thoughtworks.martdhis2sync.step;

import com.thoughtworks.martdhis2sync.processor.TrackedEntityInstanceProcessor;
import com.thoughtworks.martdhis2sync.reader.MappingReader;
import com.thoughtworks.martdhis2sync.writer.TrackedEntityInstanceWriter;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TrackedEntityInstanceStep {

    @Autowired
    private MappingReader mappingReader;

    @Autowired
    private ObjectFactory<TrackedEntityInstanceProcessor> processorObjectFactory;

    @Autowired
    private TrackedEntityInstanceWriter writer;

    @Autowired
    private StepFactory stepFactory;

    private static final String TEI_STEP_NAME = "Tracked Entity Step";

    private TrackedEntityInstanceProcessor getProcessor(Object mappingObj, List<String> searchableAttributes, List<String> comparableAttributes) {
        TrackedEntityInstanceProcessor processor = processorObjectFactory.getObject();
        processor.setMappingObj(mappingObj);
        processor.setSearchableAttributes(searchableAttributes);
        processor.setComparableAttributes(comparableAttributes);

        return processor;
    }

    public Step get(String patientId, Object mappingObj, List<String> searchableAttributes, List<String> comparableAttributes) {
        return stepFactory.build(
                TEI_STEP_NAME,
                mappingReader.getInstanceReader(patientId),
                getProcessor(mappingObj, searchableAttributes, comparableAttributes),
                writer
        );
    }
}
