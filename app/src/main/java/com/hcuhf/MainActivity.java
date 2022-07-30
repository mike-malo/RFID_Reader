package com.hcuhf;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.hc.pda.HcPowerCtrl;
import com.hcuhf.fragment.LockAndKillTagFragment;
import com.hcuhf.fragment.ReadTagFragment;
import com.hcuhf.fragment.ScanTagFragment;
import com.hcuhf.fragment.SettingFragment;
import com.hcuhf.fragment.WriteTagFragment;
import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.interfaces.BindView;
import com.kongzue.baseframework.interfaces.DarkNavigationBarTheme;
import com.kongzue.baseframework.interfaces.DarkStatusBarTheme;
import com.kongzue.baseframework.interfaces.FragmentLayout;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.interfaces.NavigationBarBackgroundColorRes;
import com.kongzue.baseframework.util.FragmentChangeUtil;
import com.kongzue.baseframework.util.JumpParameter;
import com.kongzue.baseframework.util.SettingsUtil;
import com.kongzue.tabbar.Tab;
import com.kongzue.tabbar.TabBarView;

import com.uhf.api.cls.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("NonConstantResourceId")
//设置不使用状态栏暗色文字图标样式
@DarkStatusBarTheme(false)
//设置底部导航栏背景颜色，此外还可以使用 @NavigationBarBackgroundColor 来指定 argb 颜色
@NavigationBarBackgroundColorRes(R.color.colorWhite)
//设置使用底部导航栏暗色图标样式
@DarkNavigationBarTheme(true)
//绑定子 Fragment 要显示的容器布局
@FragmentLayout(R.id.viewPager)
@Layout(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tab_bar)
    TabBarView tabBarView;
    ScanTagFragment scanTagFragment = new ScanTagFragment();
    ReadTagFragment readTagFragment = new ReadTagFragment();
    WriteTagFragment writeTagFragment = new WriteTagFragment();
    SettingFragment settingFragment = new SettingFragment();
    LockAndKillTagFragment lockAndKillTagFragment = new LockAndKillTagFragment();
    public String TAG = "TAG";
    public Reader uhfReader;

    HcPowerCtrl ctrl;
    public ArrayList<HashMap<String, String>> tagList;
    public HashMap<String, String> map;
    public MutableLiveData<Boolean> initState = new MutableLiveData<>();
    public MutableLiveData<Boolean> getInitState() { return initState; }
    public SettingsUtil settingsUtil;
    public boolean inventoryEpc = false;//盘存模式，EPC 或 TID
    public int setMaxPower = 0;
    public String module = "";
    public boolean isR2000 = true;

    @Override
    public void initViews() {
        tagList = new ArrayList<>();
        map = new HashMap<>();
    }
    public String getMD5() {
        return "";
    }
    //获取模块温度
    public int getModuleTemperature() {
        int[] val = new int[1];
        Reader.READER_ERR er = uhfReader.ParamGet(
                Reader.Mtr_Param.MTR_PARAM_RF_TEMPERATURE, val);
        if (er == Reader.READER_ERR.MT_OK_ERR) {
            return val[0];
        } else {
            return -1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsUtil = new SettingsUtil("uhf");
        inventoryEpc = settingsUtil.getBoolean("inventory", false);
        initUHF();
        initSound();
    }

    private void initUHF() {
      ctrl = new HcPowerCtrl();
        ctrl.identityPower(1);//供电
        if (uhfReader == null) {
            uhfReader = new Reader();
            new InitUHFTask().execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        exitUHF();
        Log.e(TAG, "onPause: "  );
    }

    private void exitUHF() {
//        power.PowerDown();
        ctrl.identityPower(0);
        uhfReader.CloseReader();
        uhfReader = null;
    }

    @Override
    public void initDatas(JumpParameter parameter) {
        List<Tab> tabs = new ArrayList<>();
        tabs.add(new Tab(this, getString(R.string.scanTag), R.mipmap.ic_launcher));
        tabs.add(new Tab(this, getString(R.string.readTag), R.mipmap.ic_launcher));
        tabs.add(new Tab(this, getString(R.string.writeTag), R.mipmap.ic_launcher));
        tabs.add(new Tab(this, getString(R.string.settingTag), R.mipmap.ic_launcher));
        tabs.add(new Tab(this, getString(R.string.lockAndKill), R.mipmap.ic_launcher));
        tabBarView.setTab(tabs);
    }

    @Override
    public void setEvents() {
        tabBarView.setOnTabChangeListener((v, index) -> {
            changeFragment(index);
            return false;
        });
        getFragmentChangeUtil().setOnFragmentChangeListener((index, fragment) -> tabBarView.setNormalFocusIndex(index));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown: 回车" + keyCode );
        if (keyCode == 293 && event.getRepeatCount() == 0) {
            scanTagFragment.startScanToR2000();
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_F1) {
            setMaxPower++;
            if (setMaxPower == 3) {
                setMaxPower = 0;
                Reader.AntPowerConf apcf = uhfReader.new AntPowerConf();
                apcf.antcnt = 1;
                Reader.AntPower jaap = uhfReader.new AntPower();
                jaap.antid = 1;
                jaap.readPower = (short) 3300;
                jaap.writePower = (short) 3300;
                apcf.Powers[0] = jaap;
                Reader.READER_ERR powerSet = uhfReader.ParamSet(Reader.Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf);
                if (powerSet == Reader.READER_ERR.MT_OK_ERR) {
                    toast("设置33db成功");
                } else {
                    Log.e(TAG, "err: "+powerSet );
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void initFragment(FragmentChangeUtil fragmentChangeUtil) {
        fragmentChangeUtil.addFragment(scanTagFragment, true);
        fragmentChangeUtil.addFragment(readTagFragment, true);
        fragmentChangeUtil.addFragment(writeTagFragment, true);
        fragmentChangeUtil.addFragment(settingFragment, true);
        fragmentChangeUtil.addFragment(lockAndKillTagFragment, true);
        changeFragment(0);
    }

    public class InitUHFTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
//            power = new RfidPower(RfidPower.PDATYPE.NONE, getApplicationContext());
//            if (power.PowerUp()) {
//                Reader.READER_ERR reader_err = uhfReader.InitReader_Notype("/dev/ttyS1", 1);
                Reader.READER_ERR reader_err = uhfReader.InitReader_Notype("/dev/ttysWK1", 1);
                Log.e(TAG, "doInBackground: "+reader_err );
                if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
                    initState.postValue(true);
                    return true;
                } else {
                    return false;
                }
//            } else {
//                return false;
//            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (result) {
                Reader.AntPowerConf apcf = uhfReader.new AntPowerConf();
                apcf.antcnt = 1;
                Reader.AntPowerConf apcf2 = uhfReader.new AntPowerConf();
                //获取功率
                uhfReader.ParamGet(Reader.Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf2);
                int session = settingsUtil.getInt("session", 1);
                //设置session 值
                uhfReader.ParamSet(Reader.Mtr_Param.MTR_PARAM_POTL_GEN2_SESSION, new int[]{session});
                //设置附加数据内容,如果纯盘EPC 就传null
//                if (inventoryEpc) {
                    uhfReader.ParamSet(Reader.Mtr_Param.MTR_PARAM_TAG_EMBEDEDDATA, null);
//                } else {
//                    Reader.EmbededData_ST subjoinSet = uhfReader.new EmbededData_ST();//如果不用附加数据时 需将此参数设null ,否则会影响读取效率
//                    subjoinSet.bank = 2;//附加数据的块区，值位 0,1,2,3 对应Gen2 标签的4个区
//                    subjoinSet.startaddr = 0;//附加数据的起始读地址，字节为单位
//                    subjoinSet.bytecnt = 12;//附近数据的读取长度地址，字节为单位
//                    subjoinSet.accesspwd = null;//不用密码的时候置空
//                    uhfReader.ParamSet(Reader.Mtr_Param.MTR_PARAM_TAG_EMBEDEDDATA, subjoinSet);
//                }
                Reader.HardwareDetails val = uhfReader.new HardwareDetails();
                uhfReader.GetHardwareDetails(val);
                if (val.module.toString().equals("MODOULE_SLR5300")) {
                    isR2000 = false;
                } else {
                    isR2000 = true;
                }
                Log.e(TAG, "模块: " + val.module.toString() );
                Toast.makeText(getApplicationContext(),   " 初始化成功,功率 " + apcf2.Powers[0].readPower / 100 + "db", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "初始化失败" , Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(MainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("初始化...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();

        }
    }

    private HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.beeper_short, 1));
        soundMap.put(2, soundPool.load(this, R.raw.beeper, 1));
        soundMap.put(3, soundPool.load(this, R.raw.scan_buzzer, 1));
        soundMap.put(4, soundPool.load(this, R.raw.beep330, 1));
        soundMap.put(5, soundPool.load(this, R.raw.scan_new, 1));
        soundMap.put(6, soundPool.load(this, R.raw.beep333, 1));
    }

    public void playSound(int id) {
        soundPool.play(soundMap.get(id), 1, // 左声道音量
                1, // 右声道音量
                1, // 优先级，0为最低
                0, // 循环次数，0无不循环，-1无永远循环
                1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
        );
    }
    public void exit() {
        exitUHF();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        System.exit(0);
    }


}