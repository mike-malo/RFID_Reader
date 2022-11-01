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
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.List;


public class InventoryFragment extends Fragment implements View.OnClickListener {
    private MainActivity mainActivity = null;
    private RecyclerView recyclerview = null;
    private Button btExport;
    private Button btClear, btStartStop, btnSingle;
    private MyAdapter myAdapter;
    private int count = 0;
    private long startTime;
    private TextView tvNumber, tvTime, tvCount;
    long stopTime;
    EditText et_stopTime;
    CheckBox cb_stopTime;
    private SelectEntity selectEntity = null;
    boolean needStop = false;
    private List<UHFTagEntity> tagEntityList = new ArrayList<>();
    Handler handler = new Handler(Looper.myLooper()) {
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
                myAdapter.notifyDataSetChanged();
            } else {
                handler.sendEmptyMessageDelayed(2, 100);
                long time = SystemClock.elapsedRealtime() - startTime;
                Log.e("TAG", "停止时间: " + stopTime);
                Log.e("TAG", "现在时间: " + time / 1000);
                if (needStop) {
                    if (stopTime == time / 1000) {
//                    UHFReader.getInstance().stopInventory();
                        startStop();
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
        tvCount = mainActivity.findViewById(R.id.tvCount);
        //设置LayoutManager，以LinearLayoutManager为例子进行线性布局
        recyclerview.setLayoutManager(new LinearLayoutManager(mainActivity));
        //设置分割线
        recyclerview.addItemDecoration(new DividerItemDecoration(mainActivity, LinearLayoutManager.VERTICAL));
        //创建适配器
        myAdapter = new MyAdapter(tagEntityList);
        //设置适配器
        recyclerview.setAdapter(myAdapter);

        btnSingle.setOnClickListener(this);
        btStartStop.setOnClickListener(this);
        btClear.setOnClickListener(this);
        btExport.setOnClickListener(this);


        mainActivity.setkeyDown(new MainActivity.KeyDown() {
            @Override
            public void onKeyDown(int keyCode) {
                startStop();
            }
        });

        cb_stopTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("TAG", "onCheckedChanged: " + isChecked);
                needStop = isChecked;
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btClear:
                clear();
                break;
            case R.id.btStartStop:
                // UHFReader.getInstance().setInventoryModeForPower(InventoryModeForPower.POWER_SAVING_MODE);
                startStop();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.setkeyDown(null);
    }

    private void startStop() {
        Log.e("TAG", "startStop: " + btStartStop.getText());
        Log.e("TAG", "startStop: " + getResources().getString(R.string.start));
        long time = SystemClock.elapsedRealtime() - startTime;
        if (btStartStop.getText().equals(getResources().getString(R.string.start))) {
            UHFReader.getInstance().setOnInventoryDataListener(new OnInventoryDataListener() {
                @Override
                public void onInventoryData(List<UHFTagEntity> tagEntityList) {
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
            if (booleanUHFReaderResult.getResultCode() == 0) {
                SystemClock.sleep(200);
                for (int i = 0; i < tagEntityList.size(); i++) {
                    Log.e("TAG", "标签 " + tagEntityList.get(i).getEcpHex() + " 次数: " + tagEntityList.get(i).getCount());
                }
            }
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
        myAdapter.notifyDataSetChanged();
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<UHFTagEntity> dataList;

        MyAdapter(List<UHFTagEntity> dataList) {
            this.dataList = dataList;
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
                Log.e("TAG", "onBindViewHolder: " + entity.getTidHex());
                holder.tvEPC.setText(entity.getEcpHex());
            }

            holder.tvRssi.setText(entity.getRssi() + "");
            holder.tvCount.setText(entity.getCount() + "");
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

}