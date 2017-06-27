package com.example.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Monkey D Luffy on 2017/6/5.
 */

public class register extends BaseActivity {
    public Button button_back_login,button_submit;
    public EditText re_username,re_password,re_confirmpwd;
    public ImageView register_progress;
    public Context context=this;
    public InetAddress addr;
    public byte[] userByte=new byte[100];
    public String userInfo;
    public Animation animation;
    public ActivityStack activityStack;
    public File file;
    public String FilePath;
    public Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Toast.makeText(context,"用户名已经存在",Toast.LENGTH_SHORT).show();
                    register_progress.clearAnimation();
                    break;
                case 1:
                    Toast.makeText(context,"服务器无响应",Toast.LENGTH_SHORT).show();
                    register_progress.clearAnimation();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Translucent translucent=new Translucent();
        translucent.setStatusTranslucent(getWindow());
        setContentView(R.layout.register);
        activityStack=ActivityStack.getActivityStack();
        activityStack.PushActivity(register.this);
        FilePath= Environment.getExternalStorageDirectory().toString()+"/HiFileCache";
        if(!new File(FilePath).exists()){
            new File(FilePath).mkdir();
        }
        file=new File(FilePath+"/userInfo.hi");
        init();
    }
    private void init(){
        animation= AnimationUtils.loadAnimation(context,R.anim.revolve);
        try {
            addr = InetAddress.getByName("192.168.191.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        button_submit=(Button)findViewById(R.id.button_submit);
        button_back_login=(Button)findViewById(R.id.button_back_login);
        re_username=(EditText)findViewById(R.id.re_username);
        re_password=(EditText)findViewById(R.id.re_password);
        re_confirmpwd=(EditText)findViewById(R.id.re_confirmpwd);
        register_progress=(ImageView)findViewById(R.id.register_progress);
        register_progress.setVisibility(View.INVISIBLE);
        button_back_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register.this.finish();
            }
        });
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(re_username.getText().length()>4){
                    if(re_password.getText().length()>4){
                        if(re_password.getText().toString().equals(re_confirmpwd.getText().toString())){
                            register_progress.startAnimation(animation);
                            sendUserInfo();
                        }else {
                            Toast.makeText(context,"密码不正确",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(context,"密码必须至少5位",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(context,"用户名至少5位",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void sendUserInfo(){
             userInfo=re_username.getText().toString()+"&"+re_password.getText().toString();
            new Thread(){
                @Override
                public void run() {
                    try {
                        Socket senduser = new Socket(addr, 8888);
                        OutputStream os = senduser.getOutputStream();
                        os.write(userInfo.getBytes());
                        os.flush();
                        senduser.shutdownOutput();
                        InputStream is = senduser.getInputStream();
                        byte[] ack = new byte[100];
                        is.read(ack);
                        String result = new String(ack).trim();
                        senduser.close();
                        os.close();
                        is.close();
                        if (result.equals("OK")) {
                            try {
                                FileOutputStream fileOutputStream=new FileOutputStream(file);
                                OutputStreamWriter osw=new OutputStreamWriter(fileOutputStream);
                                osw.write(userInfo);
                                osw.close();
                                fileOutputStream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("username", re_username.getText().toString());
                            intent.putExtra("password", re_password.getText().toString());
                            startActivityForResult(intent,0);
                        } else {
                            Message m = new Message();
                            m.what = 0;
                            handler.sendMessage(m);
                        }
                    }catch (ConnectException e){
                        Message m = new Message();
                        m.what = 1;
                        handler.sendMessage(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0){
            activityStack.PopActivity();
            register.this.finish();
        }
    }
}
