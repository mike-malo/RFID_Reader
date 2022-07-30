package com.hcuhf.fragment;

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

@Layout(R.layout.fragment_read_tag)
public class ReadTagFragment extends BaseFragment<MainActivity> {


    @BindView(R.id.sp_readBlank)
    AppCompatSpinner sp_readBlank;
    @BindView(R.id.bt_readTag)
    Button bt_readTag;
    @BindView(R.id.et_psw)
    EditText et_psw;
    @BindView(R.id.et_readAddress)
    EditText et_readAddress;
    @BindView(R.id.et_readLen)
    EditText et_readLen;
    @BindView(R.id.et_tagData)
    EditText et_tagData;


    @Override
    public void initViews() {

    }


    @Override
    public void initDatas() {
        sp_readBlank.setSelection(1);
    }


    @Override
    public void setEvents() {
        bt_readTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTag();
            }
        });
        sp_readBlank.setDropDownVerticalOffset(50);
        sp_readBlank.setDropDownHorizontalOffset(10);
        sp_readBlank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    et_readAddress.setText("2");
                } else   {
                    et_readAddress.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void readTag() {
        String psw = et_psw.getText().toString().trim();
        int address = Integer.parseInt(et_readAddress.getText().toString().trim());
        int len = Integer.parseInt(et_readLen.getText().toString().trim());
        byte[] readData = new byte[len * 2];
        byte[] readPsw = new byte[4];
        int blank = sp_readBlank.getSelectedItemPosition();
        if (!psw.equals("")) {
            me.uhfReader.Str2Hex(psw,psw.length(),readPsw);
        }
        int readCont = 3;
        Reader.READER_ERR reader_err;
        do {
            reader_err = me.uhfReader.GetTagData(1, (char) blank, address, len, readData, readPsw, (short) 1000);
            if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
                String data = "";
                char[] out = null;
                out = new char[readData.length * 2];
                me.uhfReader.Hex2Str(readData, readData.length, out);
                data = String.valueOf(out);
                Log.e("TAG", "readTag: 读取成功" + data);
                et_tagData.setText(data);
            }
            readCont--;
            if (readCont < 1) {
                break;
            }
            if (reader_err != Reader.READER_ERR.MT_OK_ERR) {
                et_tagData.setText("读取失败");
            }
        } while (reader_err != Reader.READER_ERR.MT_OK_ERR);
    }

}