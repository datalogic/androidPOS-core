package com.datalogic.dlapos.androidpos.common;

/**
 * Class representing a generic event data.
 */
public class EventData {
    /**
     * Error event flag.
     */
    public static final int ERROR_EVENT = 1;
    /**
     * Label event flag.
     */
    public static final int LABEL_EVENT = 2;
    /**
     * Item event flag.
     */
    public static final int ITEM_EVENT = 3;
    /**
     * System data event flag.
     */
    public static final int SYSTEM_DATA_EVENT = 4;

    private int eventType;

    /**
     * Default constructor. The event type should be one of the following:
     * <li>{@link #ERROR_EVENT ERROR_EVENT}</li>
     * <li>{@link #LABEL_EVENT LABEL_EVENT}</li>
     * <li>{@link #ITEM_EVENT ITEM_EVENT}</li>
     * <li>{@link #SYSTEM_DATA_EVENT SYSTEM_DATA_EVENT}</li>
     *
     * @param eventType the event type.
     */
    public EventData(int eventType) {
        this.eventType = eventType;
    }

    /**
     * Function to get the event type. It should be one of the following:
     * <li>{@link #ERROR_EVENT ERROR_EVENT}</li>
     * <li>{@link #LABEL_EVENT LABEL_EVENT}</li>
     * <li>{@link #ITEM_EVENT ITEM_EVENT}</li>
     * <li>{@link #SYSTEM_DATA_EVENT SYSTEM_DATA_EVENT}</li>
     *
     * @return the event type.
     */
    public int getEventType() {
        return this.eventType;
    }

    /**
     * Function to check if this is an error event.
     *
     * @return true if this is an error event.
     */
    public boolean isErrorEvent() {
        return (this.eventType == ERROR_EVENT);
    }

    /**
     * Function to check if this is an item event.
     *
     * @return true if this is an item event.
     */
    public boolean isItemEvent() {
        return (this.eventType == ITEM_EVENT);
    }

    /**
     * Function to check if this is a label event.
     *
     * @return true if this is a label event.
     */
    public boolean isLabelEvent() {
        return (this.eventType == LABEL_EVENT);
    }

    /**
     * Function to check if this is a system data event.
     *
     * @return true if this is a system data event.
     */
    public boolean isSystemDataEvent() {
        return (this.eventType == SYSTEM_DATA_EVENT);
    }

    /**
     * Function to set this event as an error event.
     */
    public void setErrorEvent() {
        setEventType(ERROR_EVENT);
    }

    /**
     * Function to set this event as an item event.
     */
    public void setItemEvent() {
        setEventType(ITEM_EVENT);
    }

    /**
     * Function to set this event as a label event.
     */
    public void setLabelEvent() {
        setEventType(LABEL_EVENT);
    }

    /**
     * Function to set this event as a system data event.
     */
    public void setSystemDataEvent() {
        setEventType(SYSTEM_DATA_EVENT);
    }

    /**
     * Function to set an event type. It should be one of the following:
     * <li>{@link #ERROR_EVENT ERROR_EVENT}</li>
     * <li>{@link #LABEL_EVENT LABEL_EVENT}</li>
     * <li>{@link #ITEM_EVENT ITEM_EVENT}</li>
     * <li>{@link #SYSTEM_DATA_EVENT SYSTEM_DATA_EVENT}</li>
     *
     * @param eventType an event type.
     */
    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
