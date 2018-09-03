package com.thoughtworks.martdhis2sync.step;

import com.thoughtworks.martdhis2sync.processor.TrackedEntityInstanceProcessor;
import com.thoughtworks.martdhis2sync.reader.MappingReader;
import com.thoughtworks.martdhis2sync.writer.TrackedEntityInstanceWriter;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class TrackedEntityInstanceStep {

    private static final String INSTANCE = "instance";

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private MappingReader mappingReader;

    @Autowired
    private ObjectFactory<TrackedEntityInstanceProcessor> processorObjectFactory;

    @Autowired
    private TrackedEntityInstanceWriter writer;

    public Step get(String lookupTable, Object mappingObj, String programName, Date syncedDate) {
        return stepBuilderFactory.get("TrackedEntityInstanceStep")
                .chunk(500)
                .reader(mappingReader.get(lookupTable, syncedDate, INSTANCE, programName))
                .processor(getProcessor(mappingObj))
                .writer(writer)
                .build();
    }

    private TrackedEntityInstanceProcessor getProcessor(Object mappingObj) {
        TrackedEntityInstanceProcessor processor = processorObjectFactory.getObject();
        processor.setMappingObj(mappingObj);

        return processor;
    }
}
