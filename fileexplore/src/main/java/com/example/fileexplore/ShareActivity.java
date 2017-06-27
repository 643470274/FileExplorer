package com.example.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by Monkey D Luffy on 2017/6/12.
 */

public class ShareActivity extends BaseActivity {
    private TextView sharedFiles,filename_share;
    private EditText friendname;
    private Button button_share;
    private String username;
    private String FP;
    private Context context=this;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Toast.makeText(context,"服务器可能炸了",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    ShareFile();
                    break;
                case 2:
                    Toast.makeText(context,"用户不存在",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Translucent().setStatusTranslucent(getWindow());
        setContentView(R.layout.activity_share);
        Intent intent=getIntent();
        username=intent.getStringExtra("username");
        FP=intent.getStringExtra("file");
        sharedFiles=(TextView)findViewById(R.id.txt_sharedfile);
        filename_share=(TextView)findViewById(R.id.filename_share);
        friendname=(EditText)findViewById(R.id.friendname);
        button_share=(Button)findViewById(R.id.button_share);
        filename_share.setText(new File(FP).getName());
        sharedFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ShareActivity.this,WebFilesActivity.class);
                intent.putExtra("function",1);
                intent.putExtra("username",username);
                startActivity(intent);
            }
        });
        button_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(friendname.getText().toString().equals(""))
                    Toast.makeText(context,"请输入朋友的用户名",Toast.LENGTH_SHORT).show();
                else {
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                byte[] ack=new byte[100];
                                String UserInfo=friendname.getText()+"&1";
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
                                if(result.equals("EXIST")){
                                    Message m=new Message();
                                    m.what=2;
                                    handler.sendMessage(m);
                                }else{
                                    Message m=new Message();
                                    m.what=1;
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
            }
        });
    }
    private void ShareFile(){
        Intent intent=new Intent(context,SendFileView.class);
        intent.putExtra("friendname",friendname.getText().toString());
        intent.putExtra("share","shared");
        intent.putExtra("file",FP);
        startActivity(intent);
    }
}
