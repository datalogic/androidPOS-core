package com.datalogic.dlapos.androidpos.common;

/**
 * Event data for items.
 */
public class ItemData extends EventData {
    private byte[] rawItemData;
    private int itemType;

    /**
     * Default constructor.
     *
     * @param rawItemData the raw item data.
     * @param itemType    the item type.
     */
    public ItemData(byte[] rawItemData, int itemType) {
        super(EventData.ITEM_EVENT);
        this.rawItemData = rawItemData;
        this.itemType = itemType;
    }

    /**
     * Function to get the item type.
     *
     * @return the item type.
     */
    public int getItemType() {
        return this.itemType;
    }

    /**
     * Function to get the raw item data.
     *
     * @return the raw item data.
     */
    public byte[] getRawItemData() {
        return this.rawItemData;
    }

    /**
     * Function to set the item type.
     *
     * @param itemType the desired item type.
     */
    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    /**
     * Function to set the raw item data.
     *
     * @param rawItemData the desired raw item data.
     */
    public void setRawItemData(byte[] rawItemData) {
        this.rawItemData = rawItemData;
    }
}
