package com.xlzn.hcpda.uhf;

import android.content.Context;

import com.xlzn.hcpda.uhf.entity.SelectEntity;
import com.xlzn.hcpda.uhf.entity.UHFReaderResult;
import com.xlzn.hcpda.uhf.entity.UHFTagEntity;
import com.xlzn.hcpda.uhf.entity.UHFVersionInfo;
import com.xlzn.hcpda.uhf.enums.ConnectState;
import com.xlzn.hcpda.uhf.enums.InventoryModeForPower;
import com.xlzn.hcpda.uhf.enums.LockActionEnum;
import com.xlzn.hcpda.uhf.enums.LockMembankEnum;
import com.xlzn.hcpda.uhf.enums.UHFSession;
import com.xlzn.hcpda.uhf.interfaces.IUHFReader;
import com.xlzn.hcpda.uhf.interfaces.OnInventoryDataListener;
import com.xlzn.hcpda.uhf.module.UHFReaderSLR;


import static com.xlzn.hcpda.uhf.entity.UHFReaderResult.ResultCode.CODE_FAILURE;
import static com.xlzn.hcpda.uhf.entity.UHFReaderResult.ResultCode.CODE_READER_NOT_CONNECTED;
import static com.xlzn.hcpda.uhf.entity.UHFReaderResult.ResultCode.CODE_SUCCESS;

public class UHFReader {

    private static UHFReader uhfReader=new UHFReader();
    private IUHFReader reader=null;
    private OnInventoryDataListener onInventoryDataListener;
    private UHFReader(){

    }

    public static UHFReader getInstance(){
            return uhfReader;
    }

    /**
     * 连接UHF读写器
     * @param context
     * @return UHFReaderResult
     */
    public synchronized UHFReaderResult<Boolean> connect(Context context) {
        if(getConnectState() == ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_SUCCESS,"模块已经连接成功,请勿重复连接!",true);
        }

        UHFReaderResult result= UHFReaderSLR.getInstance().connect(context);
        if(result.getResultCode()== CODE_SUCCESS){
            reader= UHFReaderSLR.getInstance();
            return result;
        }

        return new UHFReaderResult(CODE_FAILURE);
    }

    /**
     * 断开UHF读写器
     * @return UHFReaderResult
     */
    public synchronized UHFReaderResult<Boolean> disConnect() {

        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return  reader.disConnect();
    }

    /***
     * 开始盘点，暂时不开放
     * @param selectEntity 盘点指定标签
     * @return
     */
    public synchronized UHFReaderResult<Boolean> startInventory(SelectEntity selectEntity) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }

        if(selectEntity!=null){
            if(selectEntity.getLength()==0){
                return new UHFReaderResult<Boolean>(CODE_FAILURE,"选择的标签长度不能为0!",false);
            }
            if(selectEntity.getData()==null){
                return new UHFReaderResult<Boolean>(CODE_FAILURE,"选择的标签数据不能为null!",false);
            }
        }
        reader.setOnInventoryDataListener(onInventoryDataListener);
        return  reader.startInventory(selectEntity);
    }
    /***
     * 开始盘点
     * @return UHFReaderResult
     */
    public synchronized UHFReaderResult<Boolean> startInventory() {
        return startInventory(null);
    }

    /**
     * 停止盘点
     * @return UHFReaderResult
     */
    public synchronized UHFReaderResult<Boolean> stopInventory() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return  reader.stopInventory();
    }

    /**
     * 开始单次盘点
     * @param selectEntity 盘点指定标签
     * @return UHFReaderResult
     */
    public UHFReaderResult<UHFTagEntity> singleTagInventory(SelectEntity selectEntity) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        if(selectEntity!=null){
            if(selectEntity.getLength()==0){
                return new UHFReaderResult<UHFTagEntity>(CODE_FAILURE,"选择的标签长度不能为0!",null);
            }
            if(selectEntity.getData()==null){
                return new UHFReaderResult<UHFTagEntity>(CODE_FAILURE,"选择的标签数据不能为null!",null);
            }
        }
        return  reader.singleTagInventory(selectEntity);
    }

    /**
     * 单次盘点
     * @return UHFReaderResult
     */
    public synchronized UHFReaderResult<UHFTagEntity> singleTagInventory() {
        return singleTagInventory(null);
    }


//TODO
    /** 暂时不支持
     * 设置盘点指定标签
     * @param selectEntity 盘点指定标签
     * @return
     */
    public UHFReaderResult<Boolean> setInventorySelectEntity(SelectEntity selectEntity) {
        return reader.setInventorySelectEntity(selectEntity);
    }

    /**
     * 设置盘点TID
     * @param flag true:表示盘点tid    false:表示盘点epc
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> setInventoryTid(boolean flag){
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setInventoryTid(flag);
    }


    /**
     * 获取UHF读写器连接状态
     * @return UHFReaderResult
     */
    public synchronized ConnectState getConnectState() {
        if(reader==null){
            return ConnectState.DISCONNECT;
        }
        return reader.getConnectState();
    }

    /**
     * 获取UHF读写器版本
     * @return UHFReaderResult
     */
    public synchronized UHFReaderResult<UHFVersionInfo> getVersions() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.getVersions();
    }

    /**
     * 设置Session
     * @param vlaue seesion枚举
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> setSession(UHFSession vlaue) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setSession(vlaue);
    }

    /**
     *  获取Session
     * @return UHFReaderResult
     */
    public UHFReaderResult<UHFSession> getSession() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.getSession();
    }

    /**
     * 设置动态Target
     * @param vlaue (0,1)  00:A->B   01:B->A
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> setDynamicTarget(int vlaue) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setDynamicTarget(vlaue);
    }

    /**
     * 设置静态Target
     * @param vlaue (0,1)  00:A   01:B
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> setStaticTarget(int vlaue) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setStaticTarget(vlaue);
    }

    /**
     * 获取动态Target
     * @return   如果下标[0]:0:表示动态 下标[1]: 00:A->B  01:B->A;    如果下标[0]: 1表示静态 下标[1]:00:A   01:B
     */
    public UHFReaderResult<int[]> getTarget() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.getTarget();
    }

    /**
     * 设置盘点功耗模式
     * @param InventoryMode （快速模式、省点模式） 默认上快速模式
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> setInventoryModeForPower(InventoryModeForPower InventoryMode) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setInventoryModeForPower(InventoryMode);
    }

    /**
     * 设置盘点数据回调
     * @param onInventoryDataListener
     */
    public void setOnInventoryDataListener(OnInventoryDataListener onInventoryDataListener) {
        this.onInventoryDataListener=onInventoryDataListener;
    }

    /**
     * 设置频率
     * @param  region 北美（902-928）	0x01
     *       中国1（920-925）	0x06
     *     欧频（865-867）	0x08
     *     中国2（840-845）	0x0a
     *     全频段（840-960）	0xff
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> setFrequencyRegion(int region) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setFrequencyRegion(region);
    }

    /**
     * 获取频率
     * @return 北美（902-928）	0x01
     *       中国1（920-925）	0x06
     *      欧频（865-867）	0x08
     *      中国2（840-845）	0x0a
     *     全频段（840-960）	0xff
     */
    public UHFReaderResult<Integer> getFrequencyRegion() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.getFrequencyRegion();
    }

    /**
     * 获取温度
     * @return UHFReaderResult
     */
    public UHFReaderResult<Integer> getTemperature() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,0);
        }
        return reader.getTemperature();
    }
    /*

      membank :
        0x00 = Reserved
        0x01 = EPC
        0x02= TID
        0x03 = User Memory

     */

    /**
     * 读标签
     * @param password 密码
     * @param membank    00：保留区  1：epc  2:tid   3：UER
     * @param address  起始地址(单位：字)
     * @param wordCount 数据长度(单位：字)
     * @param selectEntity  指定标签，此参数为null表示不指定标签
     * @return UHFReaderResult
     */
    public UHFReaderResult<String> read(String password,int membank,int address,int wordCount,SelectEntity selectEntity) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult<String>(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,null);
        }
        if(selectEntity!=null){
            if(selectEntity.getLength()==0){
                return new UHFReaderResult<String>(CODE_FAILURE,"选择的标签长度不能为0!",null);
            }
            if(selectEntity.getData()==null){
                return new UHFReaderResult<String>(CODE_FAILURE,"选择的标签数据不能为null!",null);
            }
        }
        return reader.read(  password,  membank,  address,  wordCount,  selectEntity);
    }

    /**
     *写数据
     * @param password 密码
     * @param membank   00：保留区  1：epc  2:tid   3：UER
     * @param address 起始地址(单位：字)
     * @param wordCount 数据长度(单位：字)
     * @param data 要写的数据
     * @param selectEntity 指定标签，此参数为null表示不指定标签
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> write(String password, int membank, int address, int wordCount, String data, SelectEntity selectEntity) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult<Boolean>(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        if(selectEntity!=null){
            if(selectEntity.getLength()==0){
                return new UHFReaderResult(CODE_FAILURE,"选择的标签长度不能为0!",null);
            }
            if(selectEntity.getData()==null){
                return new UHFReaderResult(CODE_FAILURE,"选择的标签数据不能为null!",null);
            }
        }
        return reader.write(  password,  membank,  address,  wordCount,  data,selectEntity);
    }

    /**
     * 销毁标签
     * @param password 密码
     * @param selectEntity  指定标签，此参数为null表示不指定标签
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> kill(String password, SelectEntity selectEntity) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult<Boolean>(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        if(selectEntity!=null){
            if(selectEntity.getLength()==0){
                return new UHFReaderResult(CODE_FAILURE,"选择的标签长度不能为0!",null);
            }
            if(selectEntity.getData()==null){
                return new UHFReaderResult(CODE_FAILURE,"选择的标签数据不能为null!",null);
            }
        }
        return reader.kill(  password, selectEntity);
    }

    /**
     * 锁标签
     * @param password 密码
     * @param membankEnum 要锁带内存区
     * @param actionEnum  要执行带操作
     * @param selectEntity  指定标签，此参数为null表示不指定标签
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> lock(String password, LockMembankEnum membankEnum, LockActionEnum actionEnum, SelectEntity selectEntity) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult<Boolean>(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        if(selectEntity!=null){
            if(selectEntity.getLength()==0){
                return new UHFReaderResult(CODE_FAILURE,"选择的标签长度不能为0!",null);
            }
            if(selectEntity.getData()==null){
                return new UHFReaderResult(CODE_FAILURE,"选择的标签数据不能为null!",null);
            }
        }
        return reader.lock(  password, membankEnum,actionEnum, selectEntity);
    }

    /**
     * 设置功率
     * @param power (5-33)
     * @return UHFReaderResult
     */
    public UHFReaderResult<Boolean> setPower(int power) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setPower(power);
    }



    /**
     * 获取功率
     * @return (5-33)
     */
    public UHFReaderResult<Integer> getPower() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.getPower();
    }

    /**
     * 获取模块型号
     *
     */
    public UHFReaderResult<String> getModuleType() {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }

        return new UHFReaderResult(0);
    }

    /**
     * 设置模块型号
     *
     */
    public UHFReaderResult<Boolean> setModuleType(String moduleType) {
        if(getConnectState() != ConnectState.CONNECTED){
            return new UHFReaderResult(CODE_READER_NOT_CONNECTED, UHFReaderResult.ResultMessage.READER_NOT_CONNECTED,false);
        }
        return reader.setModuleType(moduleType);
    }





}
