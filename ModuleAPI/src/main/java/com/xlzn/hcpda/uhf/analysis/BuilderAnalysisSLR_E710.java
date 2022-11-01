package com.xlzn.hcpda.uhf.analysis;

import com.xlzn.hcpda.uhf.entity.SelectEntity;
import com.xlzn.hcpda.uhf.entity.UHFReaderResult;
import com.xlzn.hcpda.utils.DataConverter;
import com.xlzn.hcpda.utils.LoggerUtils;

import java.util.Arrays;

public class BuilderAnalysisSLR_E710 extends BuilderAnalysisSLR {

   private String TAG="BuilderAnalysisSLR_E710";
    /**
     * 获取发送盘点的指令（快速模式）
     *
     * @return return
     */
    public byte[] makeStartFastModeInventorySendData(SelectEntity selectEntity,boolean isTID) {

        if(selectEntity==null) {
            // 0xFF+DATALEN+0xAA+”Moduletech”+SubCmdHighByte+SubCmdLowByte+data+SubCrc+0xbb+CRC
            //FF+DATALEN+ 0XAA+”Moduletech”+AA+48+data+SubCrc+0xbb+CRC
            //data:N字节，2字节METADATAFLAG+1字节OPTION+2字节SEARCHFLAGS+匹配过滤相关数据（由OPTION决定，同0X22指令）+盘存嵌入命令（由SEARCHFLAGS决定，同0X22指令）
            // 1.SubCrc：1字节，为SubCmdHighByte开始到data结束的所有数据相加结果的低8位值；
            byte[] senddata = new byte[512];
            byte[] moduletech = "Moduletech".getBytes();
            //************Moduletech*************
            System.arraycopy(moduletech, 0, senddata, 0, moduletech.length);
            //***********SubCmdHighByte+SubCmdLowByte************
            int index = moduletech.length;
            int subcrcIndex = index;
            senddata[index++] = (byte) 0xAA;
            senddata[index++] = (byte) 0x58;

            for(int k=0;k<20;k++){
                senddata[index++] = (byte) 0x00;
            }

            //************data*******************
            //2字节METADATAFLAG
            final int count = 0X0001;//Bit0置位即标签在盘存时间内被盘存到的次数将会返回
            final int rssi = 0x0002;//BIT1置位即标签的RSSI信号值将会被返回
            final int ant = 0X0004;//BIT2置位即标签 被盘存到时所用的天线 ID号将会被返回。（逻辑天线号）
            final int tagData=0x0080;//返回嵌入命令内存数据
//            final int flag = count | rssi | ant | tagData;
            final int flag = rssi | ant ;
            senddata[index++] = (flag >> 8) & 0xFF;
            senddata[index++] = (byte) (flag & 0xFF);
            //1字节OPTION
            senddata[index++] = 0x00;//不启用匹配过滤
            //2字节SEARCHFLAGS,SEARCHFLAGS高字节的低4位表示不停止盘存过程中的停顿时间dd
            //0x10:每工作1秒中盘存时间950毫秒，停顿时间50毫秒
            //0x20:停顿时间100毫秒,0x30:停顿150，0x00:不停顿
            senddata[index++] = (0x00 | 0x10);
            //senddata[index++] = 0x04;//todo 0x00
            if(!isTID) {
                senddata[index++] = 0x00;
            }else {
                senddata[index++] = 0x04;
                //**********************************************
                //嵌入命令数量，目前该值只能为1.
                senddata[index++] = (byte) 0x01;
                //嵌入命令的数据域的字节长度。
                senddata[index++] = (byte) 0x09;
                //嵌入的命令码。目前只能嵌入（0X28命令）
                senddata[index++] = (byte) 0x28;
                //嵌入命令的数据域
                //Emb Cmd Timeout
                senddata[index++] = (byte) 0x00;
                senddata[index++] = (byte) 0x00;
                //Emb Cmd Option
                senddata[index++] = (byte) 0x00;
                //Read Membank
                senddata[index++] = (byte) 0x02;
                //Read Address
                senddata[index++] = (byte) 0x00;
                senddata[index++] = (byte) 0x00;
                senddata[index++] = (byte) 0x00;
                senddata[index++] = (byte) 0x00;
                //Read Word Count
                senddata[index++] = (byte) 0x06;
            }

            //****************data****************
            //****************SubCrc****************
            int subcrcTemp = 0;
            for (int k = subcrcIndex; k < index; k++) {
                subcrcTemp = subcrcTemp + (senddata[k] & 0xFF);
            }
            senddata[index++] = (byte) (subcrcTemp & 0xFF);
            //****************SubCrc****************
            senddata[index++] = (byte) 0xbb;
            return buildSendData(0XAA, Arrays.copyOf(senddata, index));
        }

        // 0xFF+DATALEN+0xAA+”Moduletech”+SubCmdHighByte+SubCmdLowByte+data+SubCrc+0xbb+CRC
        //FF+DATALEN+ 0XAA+”Moduletech”+AA+48+data+SubCrc+0xbb+CRC
        //data:N字节，2字节METADATAFLAG+1字节OPTION+2字节SEARCHFLAGS+匹配过滤相关数据（由OPTION决定，同0X22指令）+盘存嵌入命令（由SEARCHFLAGS决定，同0X22指令）
        // 1.SubCrc：1字节，为SubCmdHighByte开始到data结束的所有数据相加结果的低8位值；
        byte[] senddata = new byte[512];
        byte[] moduletech = "Moduletech".getBytes();
        //************Moduletech*************
        System.arraycopy(moduletech, 0, senddata, 0, moduletech.length);
        //***********SubCmdHighByte+SubCmdLowByte************
        int index = moduletech.length;
        int subcrcIndex = index;
        senddata[index++] = (byte) 0xAA;
        senddata[index++] = (byte) 0x58;

        for(int k=0;k<20;k++){
            senddata[index++] = (byte) 0x00;
        }
        //************data*******************
        //2字节METADATAFLAG
        final int count = 0X0001;//Bit0置位即标签在盘存时间内被盘存到的次数将会返回
        final int rssi = 0x0002;//BIT1置位即标签的RSSI信号值将会被返回
        final int ant = 0X0004;//BIT2置位即标签 被盘存到时所用的天线 ID号将会被返回。（逻辑天线号）
        //TODO 需要增加获取嵌入式命令数据, 要不然analysisFastModeTagInfoReceiveData 函数在解析数据的时候，下面这行会获取错误数据导致数组内存溢出
        //TODO int tidLen= ((taginfo[++statIndex] & 0xFF) << 8) | (taginfo[++statIndex] & 0xFF);//嵌入命令的数据
        final int tagData=0x0080;//返回嵌入命令内存数据
        final int flag = count | rssi | ant |tagData;
        senddata[index++] = (flag >> 8) & 0xFF;
        senddata[index++] = (byte)(flag & 0xFF);
        //1.字节OPTION
        senddata[index++] = (byte) (selectEntity.getOption());// 启用匹配过滤
        //2.字节SEARCHFLAGS,SEARCHFLAGS高字节的低4位表示不停止盘存过程中的停顿时间dd
        //0x10:每工作1秒中盘存时间950毫秒，停顿时间50毫秒
        //0x20:停顿时间100毫秒,0x30:停顿150，0x00:不停顿
        senddata[index++] = (0x00 | 0x10);
        senddata[index++] = 0x00;
        //3. 4字节AccessPassword
        senddata[index++] = 0x00;
        senddata[index++] = 0x00;
        senddata[index++] = 0x00;
        senddata[index++] = 0x00;
        //4. Select Address(bits)
        int address=selectEntity.getAddress();
        senddata[index++]=(byte)((address>>24) &0xFF);
        senddata[index++]=(byte)((address>>16) &0xFF);
        senddata[index++]=(byte)((address>>8) &0xFF);
        senddata[index++]=(byte)(address&0xFF);
        //Select data length(bits)
        senddata[index++]=(byte)selectEntity.getLength();
        //Select data
        byte[] byteData=DataConverter.hexToBytes(selectEntity.getData());
        int len=selectEntity.getLength()/8;
        if(selectEntity.getLength()%8!=0) {
            len += 1;
        }
        for(int k=0;k<len;k++){
            senddata[index++]=byteData[k];
        }

        //****************data****************
        //****************SubCrc****************
        int subcrcTemp = 0;
        for (int k = subcrcIndex; k < index; k++) {
            subcrcTemp = subcrcTemp + (senddata[k] & 0xFF);
        }
        senddata[index++] = (byte) (subcrcTemp & 0xFF);
        //****************SubCrc****************
        senddata[index++] = (byte) 0xbb;
        return buildSendData(0XAA, Arrays.copyOf(senddata, index));

    }

    /**
     * 解析接收的开始盘点的指令（快速模式）
     *
     * @return return
     */
    public UHFReaderResult<Boolean> analysisStartFastModeInventoryReceiveData(UHFProtocolAnalysisBase.DataFrameInfo data, boolean isTID) {
        this.isTID=isTID;
        //0xFF+DATALEN+0XAA+STATUS +”Moduletech”+SubCmdHighByte+SubCmdLowByte+data+CRC
        if (data != null) {
            if (data.status == 00) {
                LoggerUtils.d(TAG, "E710开始盘点指令返回Data:" + DataConverter.bytesToHex(data.data));
                if ((data.data[10] & 0xFF) == 0xAA && (data.data[11] & 0xFF) == 0x58) {
                    return new UHFReaderResult<Boolean>(UHFReaderResult.ResultCode.CODE_SUCCESS, "", true);
                }
                //4D 6F 64 75 6C 65 74 65 63 68 AA 48
            }
        }
        return new UHFReaderResult<Boolean>(UHFReaderResult.ResultCode.CODE_FAILURE, "", false);
    }
}
