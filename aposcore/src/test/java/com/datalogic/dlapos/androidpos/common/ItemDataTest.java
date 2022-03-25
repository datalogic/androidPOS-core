package com.datalogic.dlapos.androidpos.common;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ItemDataTest {

    private final ItemData _itemData = new ItemData(new byte[]{1}, 1);

    @Test
    public void setItemType() {
        assertThat(_itemData.getItemType()).isEqualTo(1);
        _itemData.setItemType(2);
        assertThat(_itemData.getItemType()).isEqualTo(2);
    }

    @Test
    public void setRawItemData() {
        byte[] result = _itemData.getRawItemData();
        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(1);
        _itemData.setRawItemData(new byte[]{2, 3, 4});
        result = _itemData.getRawItemData();
        assertThat(result.length).isEqualTo(3);
        assertThat(result[0]).isEqualTo(2);
        assertThat(result[1]).isEqualTo(3);
        assertThat(result[2]).isEqualTo(4);
    }
}