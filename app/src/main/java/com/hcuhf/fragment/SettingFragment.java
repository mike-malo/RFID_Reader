package com.hcuhf.fragment;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatSpinner;

import com.hcuhf.MainActivity;
import com.hcuhf.R;
import com.kongzue.baseframework.BaseFragment;
import com.kongzue.baseframework.interfaces.BindView;
import com.kongzue.baseframework.interfaces.Layout;
import com.uhf.api.cls.Reader;


@SuppressLint("NonConstantResourceId")
@Layout(R.layout.fragment_setting)
public class SettingFragment extends BaseFragment<MainActivity> {
    @BindView(R.id.sp_setWorkBand)
    AppCompatSpinner sp_frequency_band;
    @BindView(R.id.sp_setPower)
    AppCompatSpinner sp_set_power;
    @BindView(R.id.bt_setPower)
    Button bt_setPower;
    @BindView(R.id.bt_getPower)
    Button bt_getPower;
    @BindView(R.id.rg_module)
    RadioGroup rg_module;

    @BindView(R.id.bt_getFrequency)
    Button bt_getFrequency;
    @BindView(R.id.bt_getT)
    Button bt_getT;
    @BindView(R.id.et_temperature)
    EditText et_T;
    @BindView(R.id.bt_setFrequency)
    Button bt_setFrequency;
    int power = 3000;

    @Override
    public void initViews() {
        sp_set_power = me.findViewById(R.id.sp_setPower);
    }

    @Override
    public void initDatas() {
        sp_frequency_band.setDropDownVerticalOffset(50);
        sp_frequency_band.setDropDownHorizontalOffset(10);
        sp_frequency_band.setSelection(1);
        sp_set_power.setDropDownVerticalOffset(50);
        sp_set_power.setDropDownHorizontalOffset(10);
        sp_frequency_band.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //设置频段
        bt_setFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFrequency();
            }
        });
        //获取频段
        bt_getFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFrequency();
            }
        });

        //获取模块当前温度
        bt_getT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_T.setText(me.getModuleTemperature()+"");
            }
        });


        //设置功率
        bt_setPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                power = sp_set_power.getSelectedItemPosition() * 100 + 500;
                setPower(power);
            }
        });

        rg_module.check(me.inventoryEpc ? R.id.rb_epc : R.id.rb_tid);
        rg_module.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_epc:
                        me.inventoryEpc = true;
                        break;
                    case R.id.rb_tid:
                        me.inventoryEpc = false;
                        break;
                }

                //设置附加数据内容,如果纯盘EPC 就传null
                if (me.inventoryEpc) {
                    Reader.READER_ERR subjoinErr = me.uhfReader.ParamSet(Reader.Mtr_Param.MTR_PARAM_TAG_EMBEDEDDATA, null);
                } else {
                    Reader.EmbededData_ST subjoinSet = me.uhfReader.new EmbededData_ST();//如果不用附加数据时 需将此参数设null ,否则会影响读取效率
                    subjoinSet.bank = 2;//附加数据的块区，值位 0,1,2,3 对应Gen2 标签的4个区
                    subjoinSet.startaddr = 0;//附加数据的起始读地址，字节为单位
                    subjoinSet.bytecnt = 12;//附近数据的读取长度地址，字节为单位
                    subjoinSet.accesspwd = null;//不用密码的时候置空
                    Reader.READER_ERR subjoinErr = me.uhfReader.ParamSet(Reader.Mtr_Param.MTR_PARAM_TAG_EMBEDEDDATA, subjoinSet);
                }
                me.settingsUtil.commit("inventory", me.inventoryEpc);
                toast("设置成功");
            }
        });
        //获取功率
        bt_getPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPower();
            }
        });
        getPower();
    }

    private void getFrequency() {
        Reader.Region_Conf[] rcf2 = new Reader.Region_Conf[1];
        Reader.READER_ERR er = me.uhfReader.ParamGet(
                Reader.Mtr_Param.MTR_PARAM_FREQUENCY_REGION, rcf2);
        if (er == Reader.READER_ERR.MT_OK_ERR) {
            switch (rcf2[0]) {
                case RG_EU:
                    sp_frequency_band.setSelection(4);
                    break;
                case RG_EU2:
                    sp_frequency_band.setSelection(5);
                    break;
                case RG_EU3:
                    sp_frequency_band.setSelection(6);
                    break;
                case RG_KR:
                    sp_frequency_band.setSelection(3);
                    break;
                case RG_NA:
                    sp_frequency_band.setSelection(1);
                    break;
                case RG_OPEN:
                    sp_frequency_band.setSelection(9);
                    break;
                case RG_PRC2:
                    sp_frequency_band.setSelection(10);
                    break;
                default:
                    sp_frequency_band.setSelection(0);
                    break;
            }
//            toast("获取成功");
        } else {
//            toast("获取失败" + er);
        }
    }

    private void setFrequency() {
        Reader.Region_Conf rre;
        switch (sp_frequency_band.getSelectedItemPosition()) {
            case 0:
                rre = Reader.Region_Conf.RG_PRC;
                break;
            case 1:
                rre = Reader.Region_Conf.RG_NA;
                break;
            case 2:
                rre = Reader.Region_Conf.RG_NONE;
                break;
            case 3:
                rre = Reader.Region_Conf.RG_KR;
                break;
            case 4:
                rre = Reader.Region_Conf.RG_EU;
                break;
            case 5:
                rre = Reader.Region_Conf.RG_EU2;
                break;
            case 6:
                rre = Reader.Region_Conf.RG_EU3;
                break;
            case 7:
            case 8:
                rre = Reader.Region_Conf.RG_NONE;
                break;
            case 9:
                rre = Reader.Region_Conf.RG_OPEN;
                break;
            case 10:
                rre = Reader.Region_Conf.RG_PRC2;
                break;
            default:
                rre = Reader.Region_Conf.RG_NONE;
                break;
        }

        Reader.READER_ERR er = me.uhfReader.ParamSet(
                Reader.Mtr_Param.MTR_PARAM_FREQUENCY_REGION, rre);
        if (er == Reader.READER_ERR.MT_OK_ERR) {
            toast("设置成功");
        } else {
            toast("设置失败 " + er);
        }
    }

    //获取功率
    public void getPower() {
        Reader.AntPowerConf apcf2 = me.uhfReader.new AntPowerConf();
        Reader.READER_ERR er = me.uhfReader.ParamGet(
                Reader.Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf2);
        if (er == Reader.READER_ERR.MT_OK_ERR) {
            int select = (apcf2.Powers[0].readPower - 500) / 100;
            if (select == 28) {
                select = 25;
            }
            sp_set_power.setSelection(select);
//            toast("获取成功");
        } else {
//            toast("获取失败 " + er);
        }

    }

    //设置功率
    public void setPower(int setPower) {
        Reader.AntPowerConf apcf = me.uhfReader.new AntPowerConf();
        apcf.antcnt = 1;
        Reader.AntPower jaap = me.uhfReader.new AntPower();
        jaap.antid = 1;
        jaap.readPower = (short) setPower;
        jaap.writePower = (short) setPower;
        apcf.Powers[0] = jaap;
        Reader.READER_ERR powerSet = me.uhfReader.ParamSet(Reader.Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf);
        if (powerSet == Reader.READER_ERR.MT_OK_ERR) {
            toast("设置成功");
        } else {
            toast("设置失败 " + powerSet);
        }
    }


    @Override
    public void setEvents() {
    }
}