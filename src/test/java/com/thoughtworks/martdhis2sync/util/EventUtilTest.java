package com.thoughtworks.martdhis2sync.util;

import com.google.gson.JsonObject;
import com.thoughtworks.martdhis2sync.model.EventTracker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.thoughtworks.martdhis2sync.CommonTestHelper.setValueForStaticField;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BatchUtil.class)
public class EventUtilTest {

    @Before
    public void setUp() throws Exception {
        mockStatic(BatchUtil.class);
        JsonObject tableRow = getTableRow("");
        when(BatchUtil.getUnquotedString(tableRow.get("instance_id").toString())).thenReturn("nLGUkAmW1YS");
        when(BatchUtil.getUnquotedString(tableRow.get("program").toString())).thenReturn("UoZQdIJuv1R");
        when(BatchUtil.getUnquotedString(tableRow.get("event_unique_id").toString())).thenReturn("m6Yfksc81Tg");
        when(BatchUtil.getUnquotedString(tableRow.get("program_stage").toString())).thenReturn("PiGF5LQjHrW");
    }

    @Test
    public void shouldAddTheGivenObjectToExistingEventTracker() throws NoSuchFieldException, IllegalAccessException {
        JsonObject tableRow = getTableRow("8hUkh8G");
        when(BatchUtil.hasValue(tableRow.get("event_id"))).thenReturn(true);
        when(BatchUtil.getUnquotedString(tableRow.get("event_id").toString())).thenReturn("8hUkh8G");
        setValueForStaticField(EventUtil.class, "existingEventTrackers", new ArrayList<>());

        assertEquals(0, EventUtil.getExistingEventTrackers().size());

        EventUtil.addExistingEventTracker(tableRow);

        assertEquals(1, EventUtil.getExistingEventTrackers().size());
    }

    @Test
    public void shouldAddTheGivenObjectToNewEventTracker() throws NoSuchFieldException, IllegalAccessException {
        JsonObject tableRow = getTableRow("");
        when(BatchUtil.hasValue(tableRow.get("event_id"))).thenReturn(false);
        setValueForStaticField(EventUtil.class, "newEventTrackers", new ArrayList<>());

        assertEquals(0, EventUtil.getNewEventTrackers().size());

        EventUtil.addNewEventTracker(tableRow);

        assertEquals(1, EventUtil.getNewEventTrackers().size());
    }

    @Test
    public void shouldReturnCollatedList() throws NoSuchFieldException, IllegalAccessException {
        EventTracker existingTracker = mock(EventTracker.class);
        EventTracker newTracker = mock(EventTracker.class);
        List<EventTracker> existingList = Collections.singletonList(existingTracker);
        List<EventTracker> newList = Collections.singletonList(newTracker);
        setValueForStaticField(EventUtil.class, "existingEventTrackers", existingList);
        setValueForStaticField(EventUtil.class, "newEventTrackers", newList);

        List<EventTracker> eventTrackers = EventUtil.getEventTrackers();

        List<EventTracker> expected = new ArrayList<>();
        expected.add(newTracker);
        expected.add(existingTracker);
        assertEquals(expected, eventTrackers);
    }

    @Test
    public void shouldClearTheLists() throws NoSuchFieldException, IllegalAccessException {
        EventTracker existingTracker = mock(EventTracker.class);
        EventTracker newTracker = mock(EventTracker.class);
        List<EventTracker> existingList = new ArrayList<>();
        existingList.add(existingTracker);
        List<EventTracker> newList = new ArrayList<>();
        newList.add(newTracker);
        setValueForStaticField(EventUtil.class, "existingEventTrackers", existingList);
        setValueForStaticField(EventUtil.class, "newEventTrackers", newList);

        EventUtil.resetEventTrackersList();

        assertEquals(0, EventUtil.getNewEventTrackers().size());
        assertEquals(0, EventUtil.getExistingEventTrackers().size());
    }

    private JsonObject getTableRow(String eventId) {
        JsonObject tableRowObject = new JsonObject();
        tableRowObject.addProperty("event_id", eventId);
        tableRowObject.addProperty("instance_id", "nLGUkAmW1YS");
        tableRowObject.addProperty("program", "UoZQdIJuv1R");
        tableRowObject.addProperty("program_stage", "m6Yfksc81Tg");
        tableRowObject.addProperty("event_unique_id", "PiGF5LQjHrW");

        return tableRowObject;
    }
}