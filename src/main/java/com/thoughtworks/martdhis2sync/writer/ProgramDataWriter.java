package com.thoughtworks.martdhis2sync.writer;


import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgramDataWriter implements ItemWriter {

    @Override
    public void write(List items) throws Exception {
        System.out.println(items);
    }
}
