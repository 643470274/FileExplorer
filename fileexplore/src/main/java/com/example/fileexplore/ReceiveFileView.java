package com.example.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by 文成 on 2017/5/23.
 */

public class ReceiveFileView extends BaseActivity{
    ReceiveFile receiveFile;
    private Button button_ReceFile,button_Goback;
    private TextView recefilename,recefilesize,alrecefilesize;
    private Socket requestFile;
    private InetAddress addr;
    private FileOpenWays fileOpenWays;
    private boolean isOpen;//接收到的文件是否可以打开
    private Context context=this;
    private boolean exit;
    final Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    alrecefilesize.setText(creatStrSize(receiveFile.fileSize_sent));
                    break;
                case 1:
                    //Toast.makeText(ReceiveFileView.this,"文件保存在"+receiveFile.filePath,Toast.LENGTH_SHORT).show();
                    if(receiveFile.isStartReceive) {
                        alrecefilesize.setText(creatStrSize(receiveFile.fileSize_sent));
                        button_ReceFile.setText("接收");
                        isOpen = false;
                    }
                    else {
                        alrecefilesize.setText("接收完成");
                        button_ReceFile.setText("打开");
                        isOpen = true;
                        exit=true;
                    }
                    break;
                case 2:
                    setFileMessage();//设置文件信息
                    break;
                case 3:
                    Toast.makeText(ReceiveFileView.this,"服务器好像没启动一样,我也很无奈",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Translucent translucent=new Translucent();
        translucent.setStatusTranslucent(getWindow());
        setContentView(R.layout.receivefileview);
        receiveFile=ReceiveFile.getReceiveFile();
        button_ReceFile=(Button)findViewById(R.id.button_ReceFile);
        button_Goback=(Button)findViewById(R.id.button_refileGoback);
        recefilename=(TextView)findViewById(R.id.ReceFileName);
        recefilesize=(TextView)findViewById(R.id.ReceFileSize);
        alrecefilesize=(TextView)findViewById(R.id.alReceFileSize);
        fileOpenWays=new FileOpenWays();
        isOpen=false;
        exit=false;
        setButton();//按钮
        setFileMessage();//设置文件信息
        ListenerReceFileMessage();//监听文件信息
    }
    public void ListenerReceFileMessage(){
        new Thread(){
            @Override
            public void run() {
                while(true){
                    Message m=new Message();
                    m.what=2;
                    handler.sendMessage(m);
                    if(exit)break;
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    public void setFileMessage(){
        if(receiveFile.fileName_pro.equals(""))
        {
            recefilename.setText("");
            recefilesize.setText("");
            alrecefilesize.setText("你提交的文件没消息了，可能服务器炸了，我也很无奈");
            button_ReceFile.setVisibility(View.INVISIBLE);
        }else{
            button_ReceFile.setVisibility(View.VISIBLE);
            recefilename.setText(receiveFile.fileName_pro);
            recefilesize.setText(creatStrSize(receiveFile.fileSize));
            Message m=new Message();
            m.what=1;
            handler.sendMessage(m);
        }
    }
    public void setButton(){
        button_ReceFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOpen) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                addr = InetAddress.getByName("192.168.191.1");
                            }
                            catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            try {
                                requestFile = new Socket(addr, 9995);
                            }
                            catch (ConnectException e){
                                Message m=new Message();
                                m.what=3;
                                handler.sendMessage(m);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                else{
                    fileOpenWays.OpenFile(receiveFile.filePath+"/"+receiveFile.fileName_pro,Build.VERSION.SDK_INT,context);
                    ReceiveFileView.this.finish();
                    isOpen=false;
                }
            }
        });
        button_Goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    public String creatStrSize(long srv){
        String result;
        float fileSize_temp;
        if(srv>1073741824){
            fileSize_temp=srv;
            fileSize_temp=fileSize_temp/1073741824;
            result=new String(floatToString(fileSize_temp)+" GB");
        }else if(srv>1048576){
            fileSize_temp=srv;
            fileSize_temp=fileSize_temp/1048576;
            result=new String(floatToString(fileSize_temp)+" MB");
        }else if(srv>1024){
            fileSize_temp=srv;
            fileSize_temp=fileSize_temp/1024;
            result=new String(floatToString(fileSize_temp)+" KB");
        }else{
            result=new String(srv+" B");
        }
        return result;
    }
    public String floatToString(float src){
        String result,temp;
        temp=new String(src+"");
        result=temp.substring(0,temp.indexOf(".")+2);
        if(result.charAt(temp.indexOf(".")+1)=='0')
            result=result.substring(0,temp.indexOf("."));
        return result;
    }
    @Override
    public void onBackPressed() {//back
        exit=true;
        ReceiveFileView.this.finish();
    }
}
