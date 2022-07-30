package com.hcuhf.fragment;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatSpinner;

import com.hcuhf.MainActivity;
import com.hcuhf.R;
import com.kongzue.baseframework.BaseFragment;
import com.kongzue.baseframework.interfaces.BindView;
import com.kongzue.baseframework.interfaces.Layout;
import com.uhf.api.cls.Reader;

@SuppressLint("NonConstantResourceId")
@Layout(R.layout.fragment_write_tag)
public class WriteTagFragment extends BaseFragment<MainActivity> {
    @BindView(R.id.sp_writeBlank)
    AppCompatSpinner sp_writeBlank;
    @BindView(R.id.bt_writeTag)
    Button bt_writeTag;
    @BindView(R.id.et_write_psw)
    EditText et_write_psw;
    @BindView(R.id.et_writeAddress)
    EditText et_writeAddress;
    @BindView(R.id.et_writeData)
    EditText et_writeData;
    boolean isWriteEpc = true;
    @Override
    public void initViews() {
    }

    @Override
    public void initDatas() {
        sp_writeBlank.setSelection(1);
        bt_writeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWriteEpc) {
                    writeTagToEpc();
                } else {
                    writeTag();
                }
            }
        });
        sp_writeBlank.setDropDownVerticalOffset(50);
        sp_writeBlank.setDropDownHorizontalOffset(10);
        sp_writeBlank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    isWriteEpc = true;
                } else {
                    isWriteEpc = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void writeTag() {
        String psw = et_write_psw.getText().toString().trim();
        int address = Integer.parseInt(et_writeAddress.getText().toString().trim());
        byte[] writePsw = new byte[4];
        int blank = sp_writeBlank.getSelectedItemPosition();
        String writeData = et_writeData.getText().toString().trim();
        byte[] data = null;
        if (!psw.equals("")) {
            me.uhfReader.Str2Hex(psw, psw.length(), writePsw);
        }
        data = new byte[writeData.length() / 2];
        me.uhfReader.Str2Hex(writeData, writeData.length(), data);//将16进制字符串转成二进制字节数据
        if (dataCheck(writeData)) {
            Log.e("TAG", "blank: "+blank );
            Log.e("TAG", "address: "+address );
            Reader.READER_ERR reader_err = me.uhfReader.WriteTagData(1, (char) blank, address, data, data.length, writePsw, (short) 1000);
            if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
                toast("写入成功");
            } else {
                toast("写入失败，错误码 - " + reader_err);
            }
        } else {
            toast("写入数据长度必须是4的倍数，必须是16进制");
        }
    }

    //纯写EPC,会改变PC位，既盘存时的长度控制位
    private void writeTagToEpc() {
        String psw = et_write_psw.getText().toString().trim();
        byte[] writePsw = new byte[4];
        int blank = sp_writeBlank.getSelectedItemPosition();
        String writeData = et_writeData.getText().toString().trim();
        byte[] data = null;
        if (!psw.equals("")) {
            me.uhfReader.Str2Hex(psw, psw.length(), writePsw);
        }
        data = new byte[writeData.length() / 2];
        me.uhfReader.Str2Hex(writeData, writeData.length(), data);
        if (dataCheck(writeData)) {
            Reader.READER_ERR reader_err = me.uhfReader.WriteTagEpcEx(1, data, data.length, writePsw, (short) 1000);
            if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
                toast("EPC写入成功");
            } else {
                toast("写入失败，错误码 - " + reader_err);
            }
        } else {
            toast("写入数据长度必须是4的倍数，必须是16进制");
        }
    }

    //校验数据合法性，是否16进制，是否符合长度4 的倍数
    public boolean dataCheck(String data) {
        String rex = "[\\da-fA-F]*";
        if (data == null || data.isEmpty() || !data.matches(rex)) {
            return false;
        }
        if ((data.length()) % 4 != 0) {
            return false;
        }
        return true;
    }

    @Override
    public void setEvents() {

    }
}