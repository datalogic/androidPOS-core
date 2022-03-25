package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class EventDataTest {

    private final EventData _eventData = new EventData(0);

    @Test
    public void setErrorEvent() {
        assertThat(_eventData.isErrorEvent()).isFalse();
        _eventData.setErrorEvent();
        assertThat(_eventData.isErrorEvent()).isTrue();
        assertThat(_eventData.getEventType()).isEqualTo(EventData.ERROR_EVENT);
    }

    @Test
    public void setItemEvent() {
        assertThat(_eventData.isItemEvent()).isFalse();
        _eventData.setItemEvent();
        assertThat(_eventData.isItemEvent()).isTrue();
        assertThat(_eventData.getEventType()).isEqualTo(EventData.ITEM_EVENT);
    }

    @Test
    public void setLabelEvent() {
        assertThat(_eventData.isLabelEvent()).isFalse();
        _eventData.setLabelEvent();
        assertThat(_eventData.isLabelEvent()).isTrue();
        assertThat(_eventData.getEventType()).isEqualTo(EventData.LABEL_EVENT);
    }

    @Test
    public void setSystemDataEvent() {
        assertThat(_eventData.isSystemDataEvent()).isFalse();
        _eventData.setSystemDataEvent();
        assertThat(_eventData.isSystemDataEvent()).isTrue();
        assertThat(_eventData.getEventType()).isEqualTo(EventData.SYSTEM_DATA_EVENT);
    }

    @Test
    public void setEventType() {
        _eventData.setEventType(EventData.ERROR_EVENT);
        assertThat(_eventData.getEventType()).isEqualTo(EventData.ERROR_EVENT);

        _eventData.setEventType(EventData.LABEL_EVENT);
        assertThat(_eventData.getEventType()).isEqualTo(EventData.LABEL_EVENT);
    }
}