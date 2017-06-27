package com.example.fileexplore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 文成 on 2017/4/16.
 */

public class LoadPhoto {//滑动加载图片方法,异步加载
    public void loadPhoto(final Map<String, Object> fileTemp, final int k, final List<HashMap<String,Object>> datalist, final Handler handler){
        new Thread(){
            @Override
            public void run() {
                String filePath;
                HashMap<String,Object> fileResult=new HashMap<>();
                filePath=fileTemp.get("path")+"/"+fileTemp.get("text");
                /***************设置省略图***************/
                Bitmap bitmap=null;
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inJustDecodeBounds=true;
                //获取这个图片的宽和高，此时Bitmap为null
                bitmap=BitmapFactory.decodeFile(filePath,options);
                options.inJustDecodeBounds=false;
                options.inSampleSize=5;
                bitmap=BitmapFactory.decodeFile(filePath,options);
                bitmap= ThumbnailUtils.extractThumbnail(bitmap,100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                /********************End******************/
                fileResult.put("pic", bitmap);
                fileResult.put("text", fileTemp.get("text"));
                fileResult.put("path", fileTemp.get("path"));
                fileResult.put("check",fileTemp.get("check"));
                datalist.set(k, fileResult);
                Message msg = new Message();
                msg.what=0x00;
                handler.sendMessage(msg);
            }
        }.start();
    }
}
