package com.example.fileexplore;

import android.os.Environment;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 文成 on 2017/5/24.
 */

public class ReceiveFile {
    public byte[] Count_byte=new byte[10];//获取文件名长度
    public byte[] file_Name=new byte[1024];//获取文件名
    public byte[] file_con=new byte[10240];
    public byte[] empty=new byte[10240];//空字节
    public String filePath;//文件存储位置
    public String fileName_pro;//文件名
    public int fileName_Count=0;//文件名长度
    public long fileSize=0,fileSize_sent=0;//文件大小
    public Socket socket,socket_fileNameCount,socket_fileName,socket_fileSize;
    public boolean isStartReceive;
    private File cache;
    private static ReceiveFile receiveFile=new ReceiveFile();
    public ReceiveFile(){
        filePath= Environment.getExternalStorageDirectory().toString()+"/HiFileCache";
        cache=new File(filePath);
        fileName_pro="";
        new Thread(){
            @Override
            public void run() {
                initReceiveFile();
            }
        }.start();
    }
    public static ReceiveFile getReceiveFile(){
        return receiveFile;
    }
    public void initReceiveFile(){
        try {
            ServerSocket serverfileNameCount=new ServerSocket(9998);//接收文件名长度
            ServerSocket serverfileName=new ServerSocket(9997);//接受文件名
            ServerSocket serverfileSize=new ServerSocket(9996);//接受文件大小
            while(true){
                socket_fileNameCount=serverfileNameCount.accept();//接收文件名长度
                socket_fileName=serverfileName.accept();//接收文件名
                socket_fileSize=serverfileSize.accept();//接收文件长度

                if(!isStartReceive)
                {
                    isStartReceive=true;
                    try {
                        InputStream is;
                        //接收文件名字节数
                        Count_byte=empty.clone();
                        is=socket_fileNameCount.getInputStream();
                        is.read(Count_byte);
                        String Count=Str_pro(Count_byte);
                        fileName_Count=Integer.parseInt(Count);
                        //接受文件名
                        file_Name=empty.clone();
                        is=socket_fileName.getInputStream();
                        is.read(file_Name);
                        fileName_pro=new String(file_Name).trim();
                        if(!cache.exists()){
                            cache.mkdir();
                        }
                        FileOutputStream fos=new FileOutputStream(new File(filePath+"/"+fileName_pro));
                        //接受文件大小
                        Count_byte=empty.clone();
                        is=socket_fileSize.getInputStream();
                        is.read(Count_byte);
                        String Size=Str_pro(Count_byte);
                        fileSize=Long.parseLong(Size);
                        fileSize_sent=0;
                        //接收文件
                        ServerSocket serverSocket = new ServerSocket(9999);//接受文件TCP端口
                        socket=serverSocket.accept();//接收文件
                        is=socket.getInputStream();
                        int len=-1;
                        while((len=is.read(file_con))!=-1){
                            fos.write(file_con, 0, len);
                            fileSize_sent+=len;
                        }
                        fos.close();
                        serverSocket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    }
                    isStartReceive=false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String Str_pro(byte[] src){
        String result="";
        int i=0;
        for(i=0;i<src.length;i++){
            if(src[i]!=0){
                result=result+(src[i]-48);
            }else{
                break;
            }
        }
        return result;
    }
}
