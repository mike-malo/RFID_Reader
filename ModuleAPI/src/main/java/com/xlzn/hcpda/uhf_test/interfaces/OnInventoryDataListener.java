package com.xlzn.hcpda.uhf_test.interfaces;

import com.xlzn.hcpda.uhf_test.entity.UHFTagEntity;

import java.util.List;

/*
 * 监听盘点回调数据
 */
public interface OnInventoryDataListener {
    public void onInventoryData(List<UHFTagEntity> tagEntityList);
}
