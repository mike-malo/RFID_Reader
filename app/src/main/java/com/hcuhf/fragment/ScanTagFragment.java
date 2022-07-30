package com.hcuhf.fragment;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcuhf.MainActivity;
import com.hcuhf.R;
import com.hcuhf.adapter.ScanTagListAdapter;
import com.kongzue.baseframework.BaseFragment;
import com.kongzue.baseframework.interfaces.BindView;
import com.kongzue.baseframework.interfaces.Layout;
import com.uhf.api.cls.ReadExceptionListener;
import com.uhf.api.cls.ReadListener;
import com.uhf.api.cls.Reader;

import java.util.HashMap;
import java.util.Objects;

import static android.text.TextUtils.isEmpty;

@SuppressLint("NonConstantResourceId")
@Layout(R.layout.fragment_scan_tag)
public class ScanTagFragment extends BaseFragment<MainActivity> implements ReadListener, ReadExceptionListener {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bt_startScan)
    Button bt_startScan;
    @BindView(R.id.bt_stopScan)
    Button bt_stopScan;
    @BindView(R.id.bt_clearData)
    Button bt_clearData;
    @BindView(R.id.recycleView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_tag_count)
    TextView tv_tagCount;
    @BindView(R.id.cb_stopSet)
    CheckBox cb_stopSet;
    @BindView(R.id.et_stopTime)
    EditText et_stopTime;
    private ScanTagListAdapter adapter;
    boolean isReading = false;//是否正在扫描
    String TAG = "TAG";
    boolean isSetStop = false;//读取时长后停止
    @Override
    public void initViews() {
        recyclerView = findViewById(R.id.recycleView);
        adapter = new ScanTagListAdapter(me, me.tagList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(me);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(me, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void initDatas() {
        me.getInitState().observe(this, aBoolean -> {
            if (aBoolean) {
                setOnReadListener();
            }
        });

        cb_stopSet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSetStop = isChecked;
        });
    }

    public void setOnReadListener() {
        me.uhfReader.addReadListener(this);
        me.uhfReader.addReadExceptionListener(this);
    }

    private void addEPCToList(String epc) {
        if (!TextUtils.isEmpty(epc)) {
            int index = checkIsExist(epc);
            me.map = new HashMap<>();
            me.map.put("epc", epc);
            me.map.put("count", String.valueOf(1));
            if (index == -1) {
                me.tagList.add(me.map);
            } else {
                int tagcount = Integer.parseInt(Objects.requireNonNull(me.tagList.get(index).get("count")), 10) + 1;
                me.map.put("count", String.valueOf(tagcount));
                me.tagList.set(index, me.map);
            }
        }
    }

    @Override
    public void setEvents() {
        bt_startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (me.isR2000) {
                    startScanToR2000();
                } else {
                    startScanTo5300();
                }
            }
        });
        bt_stopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
            }
        });
        bt_clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                me.tagList.clear();
                handler.sendEmptyMessage(1);
            }
        });
    }

    private void startScanTo5300() {
        isReading = true;
        bt_stopScan.setEnabled(true);
        bt_startScan.setEnabled(false);
        new InventoryRawThread().start();
    }


    public void startScanToR2000() {
        if (!isReading) {
            int[] ants = new int[]{1};
            int option = 0;
            if (!me.inventoryEpc) {
                option = 32768;
            }
            Reader.READER_ERR reader_err = me.uhfReader.AsyncStartReading(ants, 1, 16);
            new InventoryThread().start();
            if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
                isReading = true;
                bt_stopScan.setEnabled(true);
                bt_startScan.setEnabled(false);
                String time = et_stopTime.getText().toString().trim();
                //是否停止扫描36333531393336323532323030343030202204000003293309290879460037480077202203005D005C1A5B215A255A265A2759285829532A4E2B002E0002
                if (isSetStop) {
                    handler.postDelayed(this::stopScan, Integer.parseInt(time)*1000);
                }
            } else {
                isReading = false;
                toast("错误码-->" + reader_err);
            }
        } else {
            stopScan();
        }
    }

    public void stopScan() {
        isReading = false;

        Reader.READER_ERR reader_err = me.uhfReader.AsyncStopReading();
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            bt_startScan.setEnabled(true);
            bt_stopScan.setEnabled(false);
        }
    }

    //标签扫描数据回调
    @Override
    public void tagRead(Reader reader, Reader.TAGINFO[] taginfos) {
//        for (int i = 0; i < taginfos.length; i++) {
//            Reader.TAGINFO taginfo = taginfos[i];
//            String epc = Reader.bytes_Hexstr(taginfo.EpcId);
//            addEPCToList(epc);
//            handler.sendEmptyMessage(1);
//            if (taginfo.EmbededDatalen > 0) {
//                char[] out = new char[taginfo.EmbededDatalen * 2];
//                me.uhfReader.Hex2Str(taginfo.EmbededData, taginfo.EmbededDatalen, out);
//                Log.e("TAG", " tid: " + String.valueOf(out));
//                addEPCToList(String.valueOf(out));
//            }
//
//            me.playSound(1);
//        }
    }

    @Override
    public void onHide() {
        if (isReading) {
            stopScan();
        }
    }
    Handler handler = new Handler(new android.os.Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    tv_tagCount.setText(getResources().getString(R.string.tagCount) + me.tagList.size());
                    adapter.notifyDataSetChanged();
                    break;
                case 3:
                    bt_startScan.setEnabled(false);
                    bt_stopScan.setEnabled(true);
                    break;
                default:
            }
            return false;
        }
    });

    //读取异常回调
    @Override
    public void tagReadException(Reader reader, Reader.READER_ERR reader_err) {
        toast("盘存异常-->> " + reader_err);
    }

    public int checkIsExist(String epc) {
        int existFlag = -1;
        if (isEmpty(epc)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < me.tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = me.tagList.get(i);
            tempStr = temp.get("epc");
            if (epc.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    public class InventoryRawThread extends Thread {
        @Override
        public void run() {
            synchronized (this) {
                while (isReading) {
                    int[] tagcnt = new int[1];
                    tagcnt[0] = 0;
                    int ants[] = {1};
                    Reader.READER_ERR reader_err = me.uhfReader.TagInventory_Raw(ants, 1, (short) 100, tagcnt);

                    if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
                        Log.e(TAG, "5300 一次标签返回个数: " +tagcnt[0] );
                        for (int i = 0; i < tagcnt[0]; i++) {
                            Reader.TAGINFO taginfo = me.uhfReader.new TAGINFO();
                            Reader.READER_ERR reader_err1 = me.uhfReader.GetNextTag(taginfo);

                            if (reader_err1 == Reader.READER_ERR.MT_OK_ERR) {
                                String epc = Reader.bytes_Hexstr(taginfo.EpcId);
                                addEPCToList(epc);
                                handler.sendEmptyMessage(1);
                                me.playSound(4);
                            } else {
                                Log.e(TAG, "5300 获取标签失败: " +reader_err1 );
                            }
                        }

                    } else {
                        handler.sendEmptyMessage(3);
                        isReading = false;
                        toast("开启失败 " + reader_err);
                        Log.e(TAG, "5300 开启失败: " +reader_err );
                    }
                }
            }
        }
    }


    public class InventoryThread extends Thread {
        @Override
        public void run() {
            int[] tagcnt = new int[1];
            synchronized (this) {
                while (isReading) {
                    Reader.READER_ERR er;
                    er = me.uhfReader.AsyncGetTagCount(tagcnt);
                    if (er == Reader.READER_ERR.MT_OK_ERR) {
                        if (tagcnt[0] > 0) {
                            for (int i = 0; i < tagcnt[0]; i++) {
                                Reader.TAGINFO taginfo = me.uhfReader.new TAGINFO();
                                er = me.uhfReader.AsyncGetNextTag(taginfo);
                                if (er == Reader.READER_ERR.MT_OK_ERR) {
//                                    if (me.inventoryEpc) {
//                                        //盘存模式 EPC
                                    String epc = Reader.bytes_Hexstr(taginfo.EpcId);
                                    addEPCToList(epc);
//                                    } else {
                                    //盘存TID模式
//                                        if (taginfo.EmbededDatalen > 0) {
//                                            char[] out = new char[taginfo.EmbededDatalen * 2];
//                                            me.uhfReader.Hex2Str(taginfo.EmbededData, taginfo.EmbededDatalen, out);
//                                            String tid = String.valueOf(out);
//                                            addEPCToList(tid);
//                                        }
//                                    }
                                    handler.sendEmptyMessage(1);
                                    me.playSound(4);
//                                    SystemClock.sleep(30);
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}