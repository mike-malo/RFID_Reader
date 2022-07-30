package com.hcuhf.fragment;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
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
@Layout(R.layout.fragment_lock_and_kill_tag)
public class LockAndKillTagFragment extends BaseFragment<MainActivity> {

    @BindView(R.id.sp_lockBlank)
    AppCompatSpinner sp_lockBlank;
    @BindView(R.id.sp_lockType)
    AppCompatSpinner sp_lockType;
    @BindView(R.id.bt_lock)
    Button bt_lock;
    @BindView(R.id.et_lock_psw)
    EditText et_lock_psw;


    @Override
    public void initViews() {

    }

    private void lockTag() {
        int lock_blank = sp_lockBlank.getSelectedItemPosition();
        int select_type = sp_lockType.getSelectedItemPosition();
        Reader.Lock_Obj lobj = null;
        Reader.Lock_Type lock_type = null;
        if (lock_blank == 0) {
            lobj = Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD;
            if (select_type == 0)
                lock_type = Reader.Lock_Type.ACCESS_PASSWD_UNLOCK;
            else if (select_type == 1)
                lock_type = Reader.Lock_Type.ACCESS_PASSWD_LOCK;
            else if (select_type == 2)
                lock_type = Reader.Lock_Type.ACCESS_PASSWD_PERM_LOCK;
        } else if (lock_blank == 1) {
            lobj = Reader.Lock_Obj.LOCK_OBJECT_KILL_PASSWORD;
            if (select_type == 0)
                lock_type = Reader.Lock_Type.KILL_PASSWORD_UNLOCK;
            else if (select_type == 1)
                lock_type = Reader.Lock_Type.KILL_PASSWORD_LOCK;
            else if (select_type == 2)
                lock_type = Reader.Lock_Type.KILL_PASSWORD_PERM_LOCK;
        } else if (lock_blank == 2) {
            lobj = Reader.Lock_Obj.LOCK_OBJECT_BANK1;
            if (select_type == 0)
                lock_type = Reader.Lock_Type.BANK1_UNLOCK;
            else if (select_type == 1)
                lock_type = Reader.Lock_Type.BANK1_LOCK;
            else if (select_type == 2)
                lock_type = Reader.Lock_Type.BANK1_PERM_LOCK;
        } else if (lock_blank == 3) {
            lobj = Reader.Lock_Obj.LOCK_OBJECT_BANK2;
            if (select_type == 0)
                lock_type = Reader.Lock_Type.BANK2_UNLOCK;
            else if (select_type == 1)
                lock_type = Reader.Lock_Type.BANK2_LOCK;
            else if (select_type == 2)
                lock_type = Reader.Lock_Type.BANK2_PERM_LOCK;
        } else if (lock_blank == 4) {
            lobj = Reader.Lock_Obj.LOCK_OBJECT_BANK3;
            if (select_type == 0)
                lock_type = Reader.Lock_Type.BANK3_UNLOCK;
            else if (select_type == 1)
                lock_type = Reader.Lock_Type.BANK3_LOCK;
            else if (select_type == 2)
                lock_type = Reader.Lock_Type.BANK3_PERM_LOCK;
        }

        String psw = et_lock_psw.getText().toString().trim();
        byte[] pwdb = new byte[4];
//        byte[] writePsw = new byte[4];
        if (!psw.equals("")) {
//            me.uhfReader.Str2Hex(psw, psw.length(), writePsw);
            me.uhfReader.Str2Hex(psw, psw.length(), pwdb);
            assert lobj != null;
            assert lock_type != null;
            Log.e("TAG", "lockTag: " +  lobj.value());
            Log.e("TAG", "lock_type: " +  lock_type.value());
            Reader.READER_ERR lock_err = me.uhfReader.LockTag(1, (byte) lobj.value(), (short) lock_type.value(), pwdb, (short) 1000);
            if (lock_err == Reader.READER_ERR.MT_OK_ERR) {
                toast("锁定成功");
            } else {
                toast("锁定失败" + lock_err);
            }
        } else {
            toast("不得使用原密码 00000000 锁定标签");
        }
    }


    @Override
    public void initDatas() {

    }


    @Override
    public void setEvents() {
        bt_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockTag();
            }
        });

    }
}