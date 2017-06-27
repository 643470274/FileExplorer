package com.example.fileexplore;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by w6434 on 2017/3/19.
 */
public class BaseActivity extends AppCompatActivity {//android申请权限封装
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public boolean hasPermission(String... permissions)
    {
        for(String permission:permissions)
        {
            if(ContextCompat.checkSelfPermission(this,permission)!=
                    PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return  true;
    }
    public void requestPermisson(int code,String...permissions)
    {
        ActivityCompat.requestPermissions(this,permissions,code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Constants.WRITE_EXTERANL_CODE:
                doSDCardpermission();
                break;
        }
    }
    public void doSDCardpermission() {

    }
}
