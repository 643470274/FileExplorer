package com.example.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by Monkey D Luffy on 2017/6/5.
 */

public class Login extends BaseActivity{
    public Button button_register,button_login;
    public EditText username,password;
    public ImageView login_progress;
    public Context context=this;
    public boolean user_com,pwd_com;
    public Animation animation;
    public ActivityStack activityStack;
    public File file;
    public String FilePath;
    public Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Toast.makeText(context,"服务器可能炸了",Toast.LENGTH_SHORT).show();
                    login_progress.clearAnimation();
                    break;
                case 1:
                    try {
                        FileOutputStream fileOutputStream=new FileOutputStream(file);
                        OutputStreamWriter osw=new OutputStreamWriter(fileOutputStream);
                        osw.write(username.getText().toString()+"&"+password.getText().toString());
                        osw.close();
                        fileOutputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    login_progress.clearAnimation();
                    Intent intent=new Intent(context,MainActivity.class);
                    intent.putExtra("username",username.getText().toString());
                    intent.putExtra("password",password.getText().toString());
                    startActivity(intent);
                    break;
                case 2:
                    Toast.makeText(context,"密码错误",Toast.LENGTH_SHORT).show();
                    login_progress.clearAnimation();
                    break;
                case 3:
                    Toast.makeText(context,"用户名不存在",Toast.LENGTH_SHORT).show();
                    login_progress.clearAnimation();
                    break;
                case 4:
                    Toast.makeText(context,"未知错误",Toast.LENGTH_SHORT).show();
                    login_progress.clearAnimation();
                    break;
            }
        }
    } ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Translucent translucent=new Translucent();
        translucent.setStatusTranslucent(getWindow());
        setContentView(R.layout.login);
        activityStack=ActivityStack.getActivityStack();
        activityStack.PushActivity(Login.this);
        init();
        loginExists();

    }
    protected void init(){
        FilePath= Environment.getExternalStorageDirectory().toString()+"/HiFileCache";
        if(!new File(FilePath).exists()){
            new File(FilePath).mkdir();
        }
        file=new File(FilePath+"/userInfo.hi");
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        button_register=(Button)findViewById(R.id.button_register);
        button_login=(Button)findViewById(R.id.button_login);
        login_progress=(ImageView)findViewById(R.id.login_progress);
        animation= AnimationUtils.loadAnimation(context,R.anim.revolve);
        login_progress.setVisibility(View.INVISIBLE);
        button_login.setEnabled(false);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,register.class);
                startActivity(intent);
            }
        });
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_progress.startAnimation(animation);
                LoginServer();
            }
        });
        username.addTextChangedListener(new TextWatcher() {//用户名输入框监听
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(username.getText().length()>4){
                    user_com=true;
                }else {
                    user_com=false;
                }
                if(user_com&&pwd_com){
                    button_login.setEnabled(true);
                }else{
                    button_login.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        password.addTextChangedListener(new TextWatcher() {//密码输入框监听
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(password.getText().length()>4){
                    pwd_com=true;
                }else {
                    pwd_com=false;
                }
                if(user_com&&pwd_com){
                    button_login.setEnabled(true);
                }else{
                    button_login.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    public void loginExists(){
        try {
            String usr,pwd,usrInfo;
            FileInputStream fileInputStream =new FileInputStream(file);
            InputStreamReader isr=new InputStreamReader(fileInputStream);
            BufferedReader br=new BufferedReader(isr);
            usrInfo=br.readLine();
            if(usrInfo!=""){
                usr=usrInfo.substring(0,usrInfo.indexOf("&"));
                pwd=usrInfo.substring(usrInfo.indexOf("&")+1);//用户输入的密码
                username.setText(usr);
                password.setText(pwd);
                login_progress.startAnimation(animation);
                LoginServer();
                Message m=new Message();
                m.what=1;
                handler.sendMessage(m);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void LoginServer(){
        new Thread(){
            @Override
            public void run() {
                try {
                    byte[] ack=new byte[100];
                    String UserInfo=username.getText().toString()+"&"+password.getText().toString();
                    Socket LoginServer=new Socket("192.168.191.1",1314);
                    OutputStream os=LoginServer.getOutputStream();
                    os.write(UserInfo.getBytes());
                    LoginServer.shutdownOutput();
                    InputStream is=LoginServer.getInputStream();
                    is.read(ack);
                    String result=new String(ack).trim();
                    os.close();
                    is.close();
                    LoginServer.close();
                    if(result.equals("OK")){
                        Message m=new Message();
                        m.what=1;
                        handler.sendMessage(m);
                    }else if(result.equals("PWDERR")){
                        Message m=new Message();
                        m.what=2;
                        handler.sendMessage(m);
                    }else if(result.equals("EXIST")){
                        Message m=new Message();
                        m.what=3;
                        handler.sendMessage(m);
                    }else{
                        Message m=new Message();
                        m.what=4;
                        handler.sendMessage(m);
                    }
                }catch (ConnectException e){
                    Message m=new Message();
                    m.what=0;
                    handler.sendMessage(m);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        activityStack.FinishAllActivity();
    }
}
