package com.example.fileexplore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Created by 文成 on 2017/5/22.
 */

public class SendFileView extends BaseActivity {
    private String FP,share;
    private TextView fileName,sendSpeed,willSend,alwaysSend,fileSize_t;
    private Button buttonGoback;
    private ProgressBar sendProgress;
    private File file;
    private Socket send_fileName_Count,send_fileName,send_fileSize,send_file,share_state;
    private InetAddress addr;
    private long fileSize,fileSize_yu,fileSize_al;
    private byte[] send_data_1=new byte[10240];//发送文件数据字节
    private byte[] Count_byte=new byte[10];//文件名或文件长度
    private File fileUser;
    private String filePath;
    private String username;
    private float DownX,DownY,MoveX,MoveY;
    private Intent intent;
    final Handler hander=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    willSend.setText("剩余\n"+creatStrSize(fileSize_yu));
                    break;
                case 1:
                    willSend.setText("发送结束");
                    break;
                case 2:
                    alwaysSend.setText("接收\n"+creatStrSize(fileSize_al));
                    break;
                case 3:
                    alwaysSend.setText("接收结束");
                    Toast.makeText(SendFileView.this,"文件也发完了,喝口凉茶静静等待简陋服务器的通知吧",Toast.LENGTH_SHORT).show();
                    break;
                case 4://传输百分进度
                    float temp=fileSize_al;
                    temp=temp/fileSize*100;
                    sendSpeed.setText(floatToString(temp)+"%");
                    sendProgress.setProgress((int) temp);
                    break;
                case 5://网络异常通知
                    Toast.makeText(SendFileView.this,"服务器好像没启动一样,我也很无奈",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Translucent translucent=new Translucent();
        translucent.setStatusTranslucent(getWindow());
        setContentView(R.layout.sendfileview);
        if(Build.VERSION.SDK_INT<21) {//判断Android版本信息，选择透明状态栏方法。
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//5.0之前透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        intent=getIntent();
        FP=intent.getStringExtra("file");
        share=intent.getStringExtra("share");
        buttonGoback=(Button)findViewById(R.id.button_sendfileGoBack);
        fileName=(TextView)findViewById(R.id.FileName);
        sendSpeed=(TextView)findViewById(R.id.SendSpeed);
        willSend=(TextView)findViewById(R.id.WillSend);
        alwaysSend=(TextView)findViewById(R.id.AlwaysSend);
        fileSize_t=(TextView)findViewById(R.id.FileSize);
        sendProgress=(ProgressBar)findViewById(R.id.SendProgress);
        file=new File(FP);
        sendProgress.setMax(100);
        fileSize_al=0;
        filePath= Environment.getExternalStorageDirectory().toString()+"/HiFileCache/";
        fileUser=new File(filePath+"userInfo.hi");
        String usrInfo;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileUser);
            InputStreamReader isr=new InputStreamReader(fileInputStream);
            BufferedReader br=new BufferedReader(isr);
            usrInfo=br.readLine();
            username=usrInfo.substring(0,usrInfo.indexOf("&"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonC();//设置按钮监听
        setTextView();//设置标题
        SendFile();//发送文件
        setWillSendSize();//设置未发送文件大小显示
        setAlwaysSendSize();//设置已发送文件大小显示
        setSendProgress();//设置进度条
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                DownX=ev.getRawX();
                DownY=ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                MoveX=ev.getRawX();
                MoveY=ev.getRawY();
                float distanceX=MoveX-DownX;
                if(distanceX>=400)finish();
                //SendFileView.this;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
    public void buttonC(){
        buttonGoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendFileView.this.finish();
            }
        });
    }
    public void setTextView(){
        fileName.setText(file.getName());
        fileSize_t.setText(creatStrSize(file.length()));
    }
    public void setSendProgress(){
        new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        sleep(1000);
                        Message m=new Message();
                        m.what=4;
                        hander.sendMessage(m);
                        if(fileSize_yu<=0)break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    public void setWillSendSize(){
        new Thread(){
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true){
                    try {
                        Message m=new Message();
                        m.what=0;
                        hander.sendMessage(m);
                        if(fileSize_yu<=0)break;
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message m=new Message();
                m.what=1;
                hander.sendMessage(m);
            }
        }.start();
    }
    public void setAlwaysSendSize(){
        new Thread(){
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true){
                    try {
                        Message m=new Message();
                        m.what=2;
                        hander.sendMessage(m);
                        if(fileSize_al>=fileSize)break;
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message m=new Message();
                m.what=3;
                hander.sendMessage(m);
            }
        }.start();
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
    public void SendFile(){
        new Thread() {
            @Override
            public void run() {
                //初始化IP地址
                try {
                    addr = InetAddress.getByName("192.168.191.1");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                try {
                    fileSize=file.length();
                    fileSize_yu=fileSize;
                    send_file=new Socket(addr,9999);
                    send_fileName_Count=new Socket(addr,9998);
                    send_fileName       =new Socket(addr,9997);
                    send_fileSize       =new Socket(addr,9996);
                    share_state         =new Socket(addr,9993);
                    OutputStream os;
                    //文件传输
                    byte[] file_Name=file.getName().getBytes();
                    //发送用户名
                    os=send_fileName_Count.getOutputStream();
                    if(share.equals("shared")){
                        os.write((username+"&"+intent.getStringExtra("friendname")).getBytes());
                    }else{
                        os.write(username.getBytes());
                    }
                    os.flush();
                    //发送文件名
                    os=send_fileName.getOutputStream();
                    os.write(file_Name);
                    os.flush();
                    //发送分享状态
                    os=share_state.getOutputStream();
                    os.write(share.getBytes());
                    os.flush();
                    //发送文件大小
                    Count_byte=LongTobyte(fileSize);
                    os=send_fileSize.getOutputStream();
                    os.write(Count_byte);
                    os.flush();
                    send_fileName_Count.close();
                    send_fileName.close();
                    send_fileSize.close();
                    share_state.close();
                    //发送文件
                    FileInputStream fileInputStream=new FileInputStream(file);
                    os=send_file.getOutputStream();
                    int len=-1;
                    fileSize_al=0;
                    while((len=fileInputStream.read(send_data_1))!=-1){
                        os.write(send_data_1,0,len);
                        fileSize_yu-=len;
                        fileSize_al+=len;
                    }
                    os.close();
                    fileInputStream.close();
                    send_file.close();
                }
                catch (ConnectException e){
                    Message m=new Message();
                    m.what=5;
                    hander.sendMessage(m);
                    SendFileView.this.finish();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }.start();
    }
    public byte[] LongTobyte(long src){
        byte[] result=new byte[10];
        String temp=new String(src+"");
        result=temp.getBytes();
        return result;
    }
    public byte[] IntTobyte(int src){
        byte[] result=new byte[10];
        String temp=new String(src+"");
        result=temp.getBytes();
        return result;
    }
}
