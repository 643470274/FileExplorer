package com.example.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Monkey D Luffy on 2017/6/12.
 */

public class WebFilesActivity extends BaseActivity {
    private float DownX,DownY,MoveX,MoveY;
    private String filepath,SysteFilePath;
    private TextView top;
    private ListView webfileAll;
    private SimpleAdapter simpleAdapter;//文件源适配器，存储文件夹内所有文件
    private SetViewBinder setViewBinder=new SetViewBinder();//适配器date数据适配类
    private List<HashMap<String,Object>> datalist;//哈希数组，存放文件名和图标
    private GetSimpleAdapterData getSimpleAdapterData=new GetSimpleAdapterData();//获取simpleadapter数据
    private List<WebFile> list;
    private Context context=this;
    private int function;
    private String username;
    private byte[] file_con=new byte[10240];
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    ParseXMLFile();
                    init();
                    break;
                case 1:
                    Toast.makeText(context,"服务器无响应",Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context,"下载完成",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    getWebFilesList();
                    break;
                case 4:
                    Toast.makeText(context,"发生未知错误",Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Translucent().setStatusTranslucent(getWindow());
        setContentView(R.layout.activity_webfiles);
        overridePendingTransition(R.anim.webfilesactivity_in,R.anim.parentactivity_out);
        Intent intent=getIntent();
        function=intent.getIntExtra("function",0);
        username=intent.getStringExtra("username");
        SysteFilePath=Environment.getExternalStorageDirectory().toString()+"/HiFileCache/";
        top=(TextView)findViewById(R.id.fromfriends);
        webfileAll=(ListView)findViewById(R.id.friends_files);
        if(function==0) {
            filepath= Environment.getExternalStorageDirectory().toString()+"/HiFileCache/datafriend.xml";
            top.setText("来自朋友的文件");
        }else if(function==1){
            filepath= Environment.getExternalStorageDirectory().toString()+"/HiFileCache/datashare.xml";
            top.setText("分享的文件");
        }
        datalist = new ArrayList<HashMap<String, Object>>();
        list=new ArrayList<WebFile>();
        EventListener();
        if(new File(filepath).exists())
        ParseXMLFile();
        init();
        getWebFilesList();
    }
    private void EventListener(){
        webfileAll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Map<String,Object> map=(Map<String,Object>)webfileAll.getAdapter().getItem(i);
                if(function==0)
                {
                    final OpenFileMenu openFileMenu=new OpenFileMenu(context,R.style.dialog_menu);
                    openFileMenu.LookOpen.setText("下载");
                    openFileMenu.LookShare.setVisibility(View.INVISIBLE);
                    openFileMenu.LookOnline.setVisibility(View.INVISIBLE);
                    openFileMenu.setOnLookOpenListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getFile(map.get("friend").toString(),map.get("path").toString());
                            Toast.makeText(context,"开始下载",Toast.LENGTH_SHORT).show();
                            openFileMenu.dismiss();
                        }
                    });
                    openFileMenu.show();
                }else if (function==1){
                    final OpenFileMenu openFileMenu=new OpenFileMenu(context,R.style.dialog_menu);
                    openFileMenu.LookOpen.setText("取消分享");
                    openFileMenu.LookShare.setVisibility(View.INVISIBLE);
                    openFileMenu.LookOnline.setVisibility(View.INVISIBLE);
                    openFileMenu.setOnLookOpenListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CancelShare(map.get("path").toString());
                            openFileMenu.dismiss();
                        }
                    });
                    openFileMenu.show();
                }
            }
        });
    }
    private void CancelShare(final String filename){
        new Thread(){
            @Override
            public void run() {
                try {
                    byte[] ack=new byte[20];
                    Socket socket=new Socket("192.168.191.1",9990);
                    OutputStream os=socket.getOutputStream();
                    os.write((filename+"&"+username).getBytes());
                    socket.shutdownOutput();
                    InputStream is=socket.getInputStream();
                    is.read(ack);
                    String ACK=new String(ack).trim();
                    if(ACK.equals("OK")){
                        Message m=new Message();
                        m.what=3;
                        handler.sendMessage(m);
                    }else{
                        Message m=new Message();
                        m.what=4;
                        handler.sendMessage(m);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void getFile(final String fileusername, final String filename){
        new Thread(){
            @Override
            public void run() {
                try {
                    Socket socket=new Socket("192.168.191.1",9991);
                    OutputStream os=socket.getOutputStream();
                    os.write((fileusername+"&"+filename).getBytes());
                    socket.shutdownOutput();
                    InputStream is=socket.getInputStream();
                    FileOutputStream fos=new FileOutputStream(new File(SysteFilePath+filename));
                    int len=-1;
                    while((len=is.read(file_con))!=-1){
                        fos.write(file_con, 0, len);
                    }
                    socket.close();
                    os.close();
                    is.close();
                    fos.close();
                    Message m=new Message();
                    m.what=2;
                    handler.sendMessage(m);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void getWebFilesList(){
        new Thread(){
            @Override
            public void run() {
                try {
                    int port=0;
                    if(function==0){
                        port=9994;
                    }else if(function==1){
                        port=9992;
                    }
                    Socket getWebFiles=new Socket("192.168.191.1",port);
                    OutputStream os=getWebFiles.getOutputStream();
                    os.write(username.getBytes());
                    getWebFiles.shutdownOutput();
                    InputStream is=getWebFiles.getInputStream();
                    FileOutputStream fos=new FileOutputStream(new File(filepath));
                    int len=-1;
                    while((len=is.read(file_con))!=-1){
                        fos.write(file_con, 0, len);
                    }
                    os.close();
                    is.close();
                    fos.close();
                    getWebFiles.close();
                    Message m=new Message();
                    m.what=0;
                    handler.sendMessage(m);
                } catch (ConnectException e){
                    Message m=new Message();
                    m.what=1;
                    handler.sendMessage(m);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void ParseXMLFile(){
        SAXParser parser = null;
        try {
            //构建SAXParser
            parser = SAXParserFactory.newInstance().newSAXParser();
            //实例化  DefaultHandler对象
            SaxParseXml parseXml=new SaxParseXml();
            //加载资源文件 转化为一个输入流
            //InputStream stream=SaxParseXml.class.getClassLoader().getResourceAsStream(filepath);
            InputStream stream= new FileInputStream(filepath);
            //调用parse()方法
            parser.parse(stream, parseXml);
            //遍历结果
            list=parseXml.getList();
            stream.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void init(){
        ArrayList<String> Path = new ArrayList<String>();
        ArrayList<String> Friends = new ArrayList<String>();
        for(WebFile webFile:list)
        {
            Path.add(webFile.getFilepath());
            Friends.add(webFile.getUsername());
        }
        datalist.clear();
        getSimpleAdapterData.getWebFileData(Path,Friends,datalist);
        if(function==0){
            simpleAdapter=new SimpleAdapter(context,datalist, R.layout.item_webfile, new String[]{"pic", "friend","path"}
                    , new int[]{R.id.pic_web, R.id.fromfriend_web,R.id.filename_web});
        }else if(function==1){
            simpleAdapter=new SimpleAdapter(context,datalist, R.layout.item_webfile, new String[]{"pic","path"}
                    , new int[]{R.id.pic_web,R.id.filename_web});
        }
        webfileAll.setAdapter(simpleAdapter);
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
                float distanceY=MoveY-DownY;
                if(distanceX<=-400&&Math.abs(distanceX)>Math.abs(distanceY)){
                    onBackPressed();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public void onBackPressed() {
        WebFilesActivity.this.finish();
        overridePendingTransition(R.anim.parentactivity_in,R.anim.webfilesactivity_out);
    }
}
