package com.xlzn.hcpda.uhf_test.ui.main;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.xlzn.hcpda.uhf_test.MainActivity;
import com.xlzn.hcpda.uhf_test.R;
import com.xlzn.hcpda.uhf_test.UHFReader;
import com.xlzn.hcpda.uhf_test.Utils;
import com.xlzn.hcpda.uhf_test.entity.UHFReaderResult;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReadFragment extends MyFragment {

    private MainActivity mainActivity;
//    private EditText etDataRead;
    private ImageView qrcodeImageView;
    private TextView time_text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_read, container, false);
    }

    @Override
    public void onKeyDownTo(int keycode) {
        super.onKeyDownTo(keycode);
        if (keycode == 290) {
            int i = 0;
            while (i < 5) {
                read();
                i++;
            }
            Log.e("TAG", "onKeyDownTo: read"  );
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
//        etDataRead = mainActivity.findViewById(R.id.etDataRead);
        qrcodeImageView = mainActivity.findViewById(R.id.qrcode_image);
        time_text = mainActivity.findViewById(R.id.textView);
    }
    public void read() {
        UHFReaderResult<String> readerResult = null;
        readerResult = UHFReader.getInstance().read("00000000", 1, 2, 6, null);
        if (readerResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
//            etDataRead.setText("");
//            Toast.makeText(mainActivity, R.string.fail, Toast.LENGTH_SHORT).show();
            return;
        }
//        Toast.makeText(mainActivity, R.string.success, Toast.LENGTH_SHORT).show();
//        etDataRead.setText(readerResult.getData());

        String inputString = readerResult.getData();
        QRCodeWriter writer = new QRCodeWriter();
        Log.d("tag", "hello?");
        try {
            BitMatrix bitMatrix = writer.encode(inputString, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK: Color.WHITE);
                }
            }
            qrcodeImageView.setImageBitmap(bitmap);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日-HH时mm分ss秒 E");
            String sim = dateFormat.format(date);
            time_text.setText(sim);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        Utils.play();
    }

}