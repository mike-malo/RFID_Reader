package com.xlzn.hcpda.uhf_test.module;

import com.xlzn.hcpda.uhf_test.enums.ConnectState;

public abstract class UHFReaderBase {
    private ConnectState connectState= ConnectState.DISCONNECT;
    protected void setConnectState(ConnectState connectState){
        this.connectState=connectState;
    }
    protected ConnectState getConnectState(){
        return connectState;
    }
}
