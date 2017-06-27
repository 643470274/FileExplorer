package com.example.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by 文成 on 2017/4/15.
 */

public class FileOpenWays {//文件打开方式
    //打开文件
    public void OpenFile(String filePath, int version, Context context){
        if(version<24)//7.0一下打开文件方式
        {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri_1 = Uri.fromFile(new
                    File(filePath));
            intent.setDataAndType (uri_1, getFilestyle(filePath));
            context.startActivity(intent);
        }else{//7.0及以上打开文件方式
            Uri uri_2= FileProvider.getUriForFile(context,"com.yll520wcf.test.fileprovider",new File(filePath));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri_2, getFilestyle(filePath));
            context.startActivity(intent);
        }

    }
    //返回文件打开格式
    public String getFilestyle(String filePath)
    {
        String fileStyle="",fileSimpleStyle="";
        int start=0;
        if(new File(filePath).getName().indexOf(".")>=0){
            for(int i=filePath.length()-1;i>=0;i--){
                if(filePath.charAt(i)=='.'){
                    start=i;
                    break;
                }
            }
            fileSimpleStyle=filePath.substring(start);
            if(fileSimpleStyle.indexOf(".mp3")>=0||fileSimpleStyle.indexOf(".MP3")>=0){
                fileStyle="audio/*";
            }else if(fileSimpleStyle.indexOf(".jpg")>=0||fileSimpleStyle.indexOf(".png")>=0){
                fileStyle="image/*";
            }else {
                fileStyle="other";
            }
        }else{
            fileStyle="other";
        }
        return fileStyle;
    }
    //返回文件格式
    public String fileMinStyle(String filePath){
        String fileSimpleStyle="";
        int start=0;
        if(new File(filePath).getName().indexOf(".")>=0){
            for(int i=filePath.length()-1;i>=0;i--){
                if(filePath.charAt(i)=='.'){
                    start=i;
                    break;
                }
            }
            fileSimpleStyle=filePath.substring(start);
            if(fileSimpleStyle.indexOf(".mp3")>=0||fileSimpleStyle.indexOf(".MP3")>=0){
                fileSimpleStyle="music";
            }else if(fileSimpleStyle.indexOf(".jpg")>=0||fileSimpleStyle.indexOf(".png")>=0){
                fileSimpleStyle="photo";
            }else {
                fileSimpleStyle="other";
            }
        }else{
            fileSimpleStyle="other";
        }
        return fileSimpleStyle;
    }
}
