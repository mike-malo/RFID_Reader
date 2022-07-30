package com.hcuhf.bean;

/**
 * ： on 2021-12-31.
 * ：630646654@qq.com
 */
public class TAGInfo {

    String epc;
    int rssi;

    public TAGInfo(String epc, int rssi) {
        this.epc = epc;
        this.rssi = rssi;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
