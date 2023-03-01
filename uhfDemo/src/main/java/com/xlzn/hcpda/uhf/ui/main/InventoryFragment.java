package com.xlzn.hcpda.uhf.ui.main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.xlzn.hcpda.jxl.FileImport;
import com.xlzn.hcpda.uhf.MainActivity;
import com.xlzn.hcpda.uhf.R;
import com.xlzn.hcpda.uhf.UHFReader;
import com.xlzn.hcpda.uhf.Utils;
import com.xlzn.hcpda.uhf.entity.SelectEntity;
import com.xlzn.hcpda.uhf.entity.UHFReaderResult;
import com.xlzn.hcpda.uhf.entity.UHFTagEntity;
import com.xlzn.hcpda.uhf.interfaces.OnInventoryDataListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class InventoryFragment extends MyFragment implements View.OnClickListener {
    private MainActivity mainActivity = null;
    private RecyclerView recyclerview = null;
    private Button btExport;
    private Button btClear, btStartStop, btnSingle;
    private MyAdapter myAdapter;
    private int count = 0;
    private long startTime;
    private TextView tvNumber, tvTime, tvCount;
    private TextView tv_80, tv_90, tv_100;
    long stopTime;
    EditText et_stopTime;
    CheckBox cb_stopTime;
    private SelectEntity selectEntity = null;
    boolean needStop = false;
    public boolean isAscii = false;
    CheckBox cb_ascii;
    private List<UHFTagEntity> tagEntityList = new ArrayList<>();
    EditText et_singleRead;

    Handler handler = new Handler(Looper.myLooper()) {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                UHFTagEntity uhfTagEntity = (UHFTagEntity) msg.obj;
                count++;
                boolean isFlag = false;
                for (UHFTagEntity entity : tagEntityList) {
                    if (entity.getEcpHex().equals(uhfTagEntity.getEcpHex())) {
                        entity.setCount(entity.getCount() + uhfTagEntity.getCount());
                        isFlag = true;
                        break;
                    }
                }
                if (!isFlag) {
                    tagEntityList.add(uhfTagEntity);
                }
                tvCount.setText(count + "");
                tvNumber.setText(tagEntityList.size() + "");
                if (tagEntityList.size() == 80) {
                    long time = SystemClock.elapsedRealtime() - startTime;
                    tv_80.setText(time+"ms");
                }
                if (tagEntityList.size() == 90) {
                    long time = SystemClock.elapsedRealtime() - startTime;
                    tv_90.setText(time+"ms");
                }
                if (tagEntityList.size() == 100) {
                    long time = SystemClock.elapsedRealtime() - startTime;
                    tv_100.setText(time+"ms");
                }
                if (myAdapter != null) {
                    myAdapter.notifyDataSetChanged();
                }

            } else {
                handler.sendEmptyMessageDelayed(2, 100);
                long time = SystemClock.elapsedRealtime() - startTime;
                Log.e("TAG", "停止时间: " + stopTime);
                Log.e("TAG", "现在时间: " + time / 1000);
                if (needStop) {
                    if (stopTime == time / 1000) {
//                    UHFReader.getInstance().stopInventory();
                        singleRead = et_singleRead.getText().toString().trim();
                        if (singleRead != null) {
                            startStop(singleRead);
                        } else {
                            startStop();
                        }
                    }
                }
                tvTime.setText(time / 1000 + "");
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        btExport = mainActivity.findViewById(R.id.btExport);
        recyclerview = mainActivity.findViewById(R.id.recyclerview);
        btClear = mainActivity.findViewById(R.id.btClear);
        btStartStop = mainActivity.findViewById(R.id.btStartStop);
        btnSingle = mainActivity.findViewById(R.id.btnSingle);
        tvNumber = mainActivity.findViewById(R.id.tvNumber);
        tvTime = mainActivity.findViewById(R.id.tvTime);
        cb_stopTime = mainActivity.findViewById(R.id.cb_stopTime);
        et_stopTime = mainActivity.findViewById(R.id.et_stopTime);
        cb_ascii = mainActivity.findViewById(R.id.cb_ascii);
        tvCount = mainActivity.findViewById(R.id.tvCount);
        tv_80 = mainActivity.findViewById(R.id.tv_80);
        tv_90 = mainActivity.findViewById(R.id.tv_90);
        tv_100 = mainActivity.findViewById(R.id.tv_100);
        et_singleRead = mainActivity.findViewById(R.id.et_singleRead);
        //设置LayoutManager，以LinearLayoutManager为例子进行线性布局
        recyclerview.setLayoutManager(new LinearLayoutManager(mainActivity));
        //设置分割线
        recyclerview.addItemDecoration(new DividerItemDecoration(mainActivity, LinearLayoutManager.VERTICAL));
//        //创建适配器
//        myAdapter = new MyAdapter(tagEntityList,isAscii);
//        //设置适配器
//        recyclerview.setAdapter(myAdapter);

        btnSingle.setOnClickListener(this);
        btStartStop.setOnClickListener(this);
        btClear.setOnClickListener(this);
        btExport.setOnClickListener(this);

        myAdapter = new MyAdapter(tagEntityList, isAscii);
        //设置适配器
        recyclerview.setAdapter(myAdapter);
        Log.e("TAG", "onActivityCreated: 盘");
        cb_stopTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("TAG", "onCheckedChanged: " + isChecked);
                needStop = isChecked;
            }
        });

        cb_ascii.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAscii = isChecked;
            }
        });
    }

    @Override
    public void onKeyDownTo(int keycode) {
        super.onKeyDownTo(keycode);
        singleRead = et_singleRead.getText().toString().trim();
        if (singleRead != null) {
            startStop(singleRead);
        } else {
            startStop();
        }
    }

    String singleRead = "";
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btClear:
                clear();
                break;
            case R.id.btStartStop:
                // UHFReader.getInstance().setInventoryModeForPower(InventoryModeForPower.POWER_SAVING_MODE);
                singleRead = et_singleRead.getText().toString().trim();
                if (singleRead != null) {
                    startStop(singleRead);
                } else {
                    startStop();
                }

                break;
            case R.id.btExport:
                new XPopup.Builder(mainActivity).asInputConfirm("名称相同将覆盖之前的文件", "导出文件根目录Download下", "epc", new OnInputConfirmListener() {
                    @Override
                    public void onConfirm(String text) {
                        String fileName = text.trim();
                        if (TextUtils.isEmpty(fileName)) {
                            fileName = "epc.xls";
                        } else {
                            fileName = fileName + ".xls";
                        }
                        new ExPortTask().execute(fileName);

                    }
                }).show();

                break;
            case R.id.btnSingle:
                UHFReaderResult<UHFTagEntity> uhfTagEntityUHFReaderResult = null;
                uhfTagEntityUHFReaderResult = UHFReader.getInstance().singleTagInventory();
                if (uhfTagEntityUHFReaderResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
                    Toast.makeText(getActivity(), R.string.success, Toast.LENGTH_SHORT).show();
                    return;
                }
                Message message = new Message();
                message.what = 1;
                message.obj = uhfTagEntityUHFReaderResult.getData();
                handler.sendMessage(message);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        btStartStop.setText(R.string.starts);
        UHFReader.getInstance().stopInventory();
        handler.removeMessages(2);
    }

    private void startStop(final String singleRead) {
        Log.e("TAG", "startStop: " + btStartStop.getText());
        Log.e("TAG", "startStop: " + getResources().getString(R.string.start));
        long time = SystemClock.elapsedRealtime() - startTime;
        if (btStartStop.getText().equals(getResources().getString(R.string.start))) {
            UHFReader.getInstance().setOnInventoryDataListener(new OnInventoryDataListener() {
                @Override
                public void onInventoryData(List<UHFTagEntity> tagEntityList) {
                    Log.e("UTU", "onInventoryData:一次回调--------  " + tagEntityList.size());
                    if (tagEntityList != null && tagEntityList.size() > 0) {
                        for (int k = 0; k < tagEntityList.size(); k++) {
                            if (!TextUtils.isEmpty(tagEntityList.get(k).getEcpHex())) {
                                if (tagEntityList.get(k).getEcpHex().startsWith(singleRead)) {
                                    Message message = new Message();
                                    message.what = 1;
                                    message.obj = tagEntityList.get(k);
                                    handler.sendMessage(message);
                                    Utils.play();
                                }
                            }
                        }
                    }
                }
            });
            //创建适配器
            myAdapter = new MyAdapter(tagEntityList, isAscii);
            //设置适配器
            recyclerview.setAdapter(myAdapter);
            UHFReaderResult<Boolean> readerResult = UHFReader.getInstance().startInventory();
            if (readerResult.getData()) {
                String trim = et_stopTime.getText().toString().trim();
                if (trim.equals("")) {
                    trim = "800000";
                }
                stopTime = Long.parseLong(trim);
                handler.sendEmptyMessageDelayed(2, 100);
                startTime = SystemClock.elapsedRealtime();
                btStartStop.setText(R.string.stop);
                btnSingle.setEnabled(false);

            } else {
                Toast.makeText(mainActivity, R.string.fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            btStartStop.setText(R.string.start);
            btnSingle.setEnabled(true);
            UHFReaderResult<Boolean> booleanUHFReaderResult = UHFReader.getInstance().stopInventory();
//            if (booleanUHFReaderResult.getResultCode() == 0) {
//                SystemClock.sleep(200);
//                for (int i = 0; i < tagEntityList.size(); i++) {
//                    Log.e("TAG", "标签 " + tagEntityList.get(i).getEcpHex() + " 次数: " + tagEntityList.get(i).getCount());
//                }
//            }
            handler.removeMessages(2);
        }
    }

    private void startStop() {
        Log.e("TAG", "startStop: " + btStartStop.getText());
        Log.e("TAG", "startStop: " + getResources().getString(R.string.start));
        long time = SystemClock.elapsedRealtime() - startTime;
        if (btStartStop.getText().equals(getResources().getString(R.string.start))) {
            UHFReader.getInstance().setOnInventoryDataListener(new OnInventoryDataListener() {
                @Override
                public void onInventoryData(List<UHFTagEntity> tagEntityList) {
                    Log.e("UTU", "onInventoryData:一次回调--------  " + tagEntityList.size());
                    if (tagEntityList != null && tagEntityList.size() > 0) {
                        for (int k = 0; k < tagEntityList.size(); k++) {
                            if (!TextUtils.isEmpty(tagEntityList.get(k).getEcpHex())) {

                                Message message = new Message();
                                message.what = 1;
                                message.obj = tagEntityList.get(k);
                                handler.sendMessage(message);
                                Utils.play();
                            }
                        }
                    }
                }
            });
            //创建适配器
            myAdapter = new MyAdapter(tagEntityList, isAscii);
            //设置适配器
            recyclerview.setAdapter(myAdapter);
            UHFReaderResult<Boolean> readerResult = UHFReader.getInstance().startInventory();
            if (readerResult.getData()) {
                String trim = et_stopTime.getText().toString().trim();
                if (trim.equals("")) {
                    trim = "800000";
                }
                stopTime = Long.parseLong(trim);
                handler.sendEmptyMessageDelayed(2, 100);
                startTime = SystemClock.elapsedRealtime();
                btStartStop.setText(R.string.stop);
                btnSingle.setEnabled(false);

            } else {
                Toast.makeText(mainActivity, R.string.fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            btStartStop.setText(R.string.start);
            btnSingle.setEnabled(true);
            UHFReaderResult<Boolean> booleanUHFReaderResult = UHFReader.getInstance().stopInventory();
//            if (booleanUHFReaderResult.getResultCode() == 0) {
//                SystemClock.sleep(200);
//                for (int i = 0; i < tagEntityList.size(); i++) {
//                    Log.e("TAG", "标签 " + tagEntityList.get(i).getEcpHex() + " 次数: " + tagEntityList.get(i).getCount());
//                }
//            }
            handler.removeMessages(2);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clear() {
        tagEntityList.clear();
        tvNumber.setText("0");
        tvTime.setText("0");
        tvCount.setText("0");
        startTime = SystemClock.elapsedRealtime();
        count = 0;
        if (myAdapter != null) {

            myAdapter.notifyDataSetChanged();
        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<UHFTagEntity> dataList;
        boolean isAsciiMy;

        MyAdapter(List<UHFTagEntity> dataList, boolean isAscii) {
            this.dataList = dataList;
            this.isAsciiMy = isAscii;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UHFTagEntity entity = dataList.get(position);
            holder.tvID.setText((position + 1) + "");
            if (entity.getTidHex() != null) {
                holder.tvEPC.setText("EPC:" + entity.getEcpHex() + "\nTID:" + entity.getTidHex());
            } else {
                if (isAsciiMy) {
                    holder.tvEPC.setText(decode(entity.getEcpHex()));
                } else {
                    String ecpHex = entity.getEcpHex();
                    holder.tvEPC.setText((ecpHex));
                }
            }
            holder.tvRssi.setText(entity.getRssi() + "");
            holder.tvCount.setText(entity.getCount() + "");
        }

        private String getProduct(String ecpHex) {
            if (ecpHex.equals("0001")) {
                return "粗加工间-粘捕式灭蝇灯-1";
            }
            if (ecpHex.equals("0002")) {
                return "粗加工间-粘捕式灭蝇灯-2";
            }
            if (ecpHex.equals("0003")) {
                return "粗加工间-双门平台冷藏雪柜";
            }
            if (ecpHex.equals("0004")) {
                return "粗加工间-单星盆台";
            }
            if (ecpHex.equals("0005")) {
                return "粗加工间-双通趟门热柜";
            }
            if (ecpHex.equals("0006")) {
                return "粗加工间-四门高身冷藏雪柜";
            }
            if (ecpHex.equals("0007")) {
                return "粗加工间-挂墙热水器";
            }
            if (ecpHex.equals("0008")) {
                return "粗加工间-制冰机";
            }
            if (ecpHex.equals("0009")) {
                return "粗加工间-全自动牛肉丸机";
            }
            if (ecpHex.equals("0010")) {
                return "粗加工间-UV过滤器";
            }
            if (ecpHex.equals("0011")) {
                return "粗加工间-挂墙消毒刀箱";
            }
            if (ecpHex.equals("0012")) {
                return "粗加工间-收污台";
            }
            if (ecpHex.equals("0013")) {
                return "粗加工间-独立空调";
            }
            if (ecpHex.equals("0014")) {
                return "粗加工间-双层工作台";
            }
            if (ecpHex.equals("0015")) {
                return "粗加工间-双星盆台";
            }
            if (ecpHex.equals("0016")) {
                return "粗加工间-挂墙消毒刀箱";
            }
            if (ecpHex.equals("0017")) {
                return "熟食区-四门高身冷藏雪柜-1";
            }
            if (ecpHex.equals("0018")) {
                return "熟食区-四门高身冷藏雪柜-2";
            }
            if (ecpHex.equals("0019")) {
                return "熟食区-双通趟门热柜-1";
            }
            if (ecpHex.equals("0020")) {
                return "熟食区-双通趟门热柜-2";
            }
            if (ecpHex.equals("0021")) {
                return "熟食区-粘捕式灭蝇灯-1";
            }
            if (ecpHex.equals("0022")) {
                return "熟食区-粘捕式灭蝇灯-2";
            }
            if (ecpHex.equals("0023")) {
                return "熟食区-制冰机";
            }
            if (ecpHex.equals("0024")) {
                return "熟食区-UV过滤器";
            }
            if (ecpHex.equals("0025")) {
                return "熟食区-双星盆台";
            }
            if (ecpHex.equals("0026")) {
                return "熟食区-独立空调";
            }
            if (ecpHex.equals("0027")) {
                return "熟食区-不锈钢传菜窗";
            }
            if (ecpHex.equals("0028")) {
                return "熟食区-挂墙烧腊展示架";
            }
            if (ecpHex.equals("0029")) {
                return "熟食区-嵌入式电磁单头平头炉";
            }
            if (ecpHex.equals("0030")) {
                return "熟食区-油网烟罩";
            }

            return ecpHex;
        }

        @Override
        public int getItemCount() {
            return dataList == null ? 0 : dataList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvID;
            private TextView tvEPC;
            private TextView tvRssi;
            private TextView tvCount;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvID = itemView.findViewById(R.id.tvID);
                tvEPC = itemView.findViewById(R.id.tvEPC);
                tvRssi = itemView.findViewById(R.id.tvRssi);
                tvCount = itemView.findViewById(R.id.tvCount);
            }
        }
    }

    public class ExPortTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            return FileImport.daochu(params[0], tagEntityList);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(mainActivity, "导出成功", Toast.LENGTH_SHORT).show();
                clear();
            } else {
                Toast.makeText(mainActivity, R.string.fail, Toast.LENGTH_SHORT).show();
            }
            progressDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(mainActivity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("导出中...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }


    /*
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String encode(String str) {
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    private static String hexString = "0123456789ABCDEFabcdef";

    /*
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节 5731363939303030323334
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

}