package com.example.fileexplore;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MainActivity extends BaseActivity  {
    private TextView text_first,path_textView;//1.标题字段；2.路径字段
    private EditText editText_search;//搜索框
    private Button button_search,show_all_button,previos_button,clear,looknotice,button_exit,button_systemfile;//1.搜索2.刷新3.上一页
    private ImageView new_notification;
    private ListView content_listView;//文件展示区
    private SimpleAdapter simpleAdapter;//文件源适配器，存储文件夹内所有文件
    private SetViewBinder setViewBinder=new SetViewBinder();//适配器date数据适配类
    private FileOpenWays fileOpenWays=new FileOpenWays();//文件类型类
    private GetSimpleAdapterData getSimpleAdapterData=new GetSimpleAdapterData();////获取simpleadapter数据
    private SetStatusBar setStatusBar=new SetStatusBar();//设置状态栏类
    private LoadPhoto loadPhoto=new LoadPhoto();//异步加载图片
    private List<HashMap<String,Object>> datalist,datalist_2;//哈希数组，存放文件名和图标
    private String FP;//全局文件路径
    private File[] files;//全局文件夹内文件数组
    private File temp,cacheFile;//递归搜索文件路径缓存
    private Context context=this;
    private FileSystemDate fileSystemDate;//文件树
    private ArrayList<String>SearchResult;//搜索结果存储
    private String word_search;//搜索关键字
    private int minPosition=0;//异步加载图片最小位置记录
    private int maxPosition=0;//异步加载图片最大位置记录
    private boolean loadImagePress=true;//允许加载图片
    private boolean firstOpen=true;//第一次打开页面
    private Dialog progressDialog = null;//搜索文件提示窗口
    private int previous_i;//上个位置
    private Stack<Integer> previous_position;
    private ServerSocket notic;
    private Socket socket;
    private ReceiveFile receiveFile;
    private InetAddress Server_IP;
    private String username,password;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;
    private ActivityStack activityStack;
    private float DownX,DownY,MoveX,MoveY;
    final Handler defineHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x00:
                    simpleAdapter.notifyDataSetChanged();
                    break;
                case 0x01:
                    button_search.setClickable(true);
                    break;
                case 0x02://显示搜索结果
                    datalist.clear();
                    for(int i=0;i<datalist_2.size();i++){
                        datalist.add(i,datalist_2.get(i));
                    }
                    simpleAdapter.notifyDataSetChanged();
                    minPosition=0;
                    maxPosition=0;
                    loadImagePress=true;
                    path_textView.setText("搜索结果");
                    SearchResult.clear();
                    progressDialog.dismiss();
                    break;
                case 0x03://显示新消息角标
                    new_notification.setVisibility(View.VISIBLE);
                    notificationManager.notify(1,notification.build());
                    break;
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Translucent translucent=new Translucent();
        translucent.setStatusTranslucent(getWindow());
        setContentView(R.layout.activity_main);
        //MIUI font_white
        setStatusBar.MIUISetStatusBarLightMode(this.getWindow(),false);
        //Flyme font_white
        setStatusBar.FlymeSetStatusBarLightMode(this.getWindow(), false);
        cacheFile=new File(Environment.getExternalStorageDirectory().toString()+"/HiFileCache");
        text_first=(TextView)findViewById(R.id.text_first);
        path_textView=(TextView)findViewById(R.id.path_textView);
        editText_search=(EditText)findViewById(R.id.editText);
        button_search=(Button)findViewById(R.id.button_search);
        show_all_button=(Button)findViewById(R.id.show_all_button);
        previos_button=(Button)findViewById(R.id.previous_button);
        clear=(Button)findViewById(R.id.clear);
        looknotice=(Button)findViewById(R.id.button_notice);
        button_exit=(Button)findViewById(R.id.button_exit);
        button_systemfile=(Button)findViewById(R.id.system_file);
        new_notification=(ImageView)findViewById(R.id.new_notification);
        new_notification.setVisibility(View.INVISIBLE);
        content_listView=(ListView)findViewById(R.id.content_list);
        receiveFile=ReceiveFile.getReceiveFile();//建立单例对象接收文件
        Intent intent=getIntent();
        username=intent.getStringExtra("username");
        password=intent.getStringExtra("password");
        text_first.setText("Hi，"+username);
        activityStack=ActivityStack.getActivityStack();
        activityStack.PushActivity(MainActivity.this);
        notificationManager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notification=new NotificationCompat.Builder(context);
        notification.setSmallIcon(R.drawable.app_icon);
        notification.setContentTitle("Hi File");
        notification.setContentText("您的文件来了，接好！！");
        notification.setDefaults(Notification.DEFAULT_ALL);
        notification.setAutoCancel(true);
        Intent reIntent=new Intent(context,ReceiveFileView.class);
        reIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(reIntent);
        PendingIntent pendingIntent=stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);
        try {
            Server_IP=InetAddress.getByName("192.168.191.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        SearchResult=new ArrayList<String>();
        previous_position=new Stack<Integer>();
        ListenerNewNotification();
        //提示存储器权限
        if(Build.VERSION.SDK_INT>=23) {
            if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                doSDCardpermission();
            } else {
                requestPermisson(Constants.WRITE_EXTERANL_CODE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }else{
            doSDCardpermission();
        }
    }
    public void ListenerNewNotification() {
        new Thread() {
            @Override
            public void run() {
                try {
                    notic = new ServerSocket(9995);
                    while (true) {
                        socket = notic.accept();
                        if (socket.getInetAddress().getHostAddress().equals("192.168.191.1")) {
                            Message m = new Message();
                            m.what = 0x03;
                            defineHandler.sendMessage(m);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
                if(distanceX>=400&&distanceX>Math.abs(distanceY)){
                    Intent intent=new Intent(context,WebFilesActivity.class);
                    intent.putExtra("function",0);
                    intent.putExtra("username",username);
                    startActivity(intent);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
    //安卓6.0及以上都存储器方法重写
    @Override
    public void doSDCardpermission() {
        CreateFileTree();//建立文件树
        ArrayList<String> genPath = new ArrayList<String>();
        ArrayList<String> genName = new ArrayList<String>();
        FP = Environment.getExternalStorageDirectory().toString();
        files = new File(Environment.getExternalStorageDirectory().toString()).listFiles();//遍历根目录
        for (File file : files)//将根目录名字加入容器
        {
            genPath.add(file.getParent());
            genName.add(file.getName());
        }
        path_textView.setText(Environment.getExternalStorageDirectory().toString().replace("/",">"));//显示根目录路径
        datalist = new ArrayList<HashMap<String, Object>>();
        datalist_2 = new ArrayList<HashMap<String, Object >>();
        getSimpleAdapterData.getData(genPath,genName,datalist);
        simpleAdapter = new SimpleAdapter(this, datalist, R.layout.item, new String[]{"pic", "text","path"}, new int[]{R.id.pic, R.id.text,0});
         /*
        调整simpleAdapter内View数据源
        */
        simpleAdapter=setViewBinder.setviewbinder(simpleAdapter);
        content_listView.setAdapter(simpleAdapter);
        genPath.clear();
        genName.clear();
        //按钮集
        ButtonCollect();
    }
    public void CreateFileTree(){
        //建立文件树
        button_search.setClickable(false);
        new Thread(){
            public void run()
            {
                fileSystemDate=new FileSystemDate(Environment.getExternalStorageDirectory().toString());
                fileSystemDate=initFileTree(Environment.getExternalStorageDirectory().toString(),fileSystemDate);
                Message msg=new Message();
                msg.what=0x01;
                defineHandler.sendMessage(msg);

            }
        }.start();
    }
    //按钮集函数
    public void ButtonCollect()
    {
        button_systemfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                files = cacheFile.listFiles();
                FP=cacheFile.getAbsolutePath();
                if(files!=null) {
                    ArrayList<String> nextPath = new ArrayList<String>();
                    ArrayList<String> nextName = new ArrayList<String>();
                    for (File file : files) {
                        nextPath.add(file.getParent());
                        nextName.add(file.getName());
                    }
                    getSimpleAdapterData.getData(nextPath, nextName, datalist);
                    simpleAdapter.notifyDataSetChanged();
                    minPosition = 0;
                    maxPosition = 0;
                    loadImagePress = true;
                    path_textView.setText(FP.replace("/", ">"));
                    nextPath.clear();
                    nextName.clear();
                    content_listView.setSelection(0);//初始位置
                    previous_position.push(previous_i);
                    path_textView.setText(FP.replace("/",">"));
                }
            }
        });
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File userfile=new File(Environment.getExternalStorageDirectory().toString()+"/HiFileCache/userInfo.hi");
                userfile.delete();
                Intent intent=new Intent(context,Login.class);
                startActivity(intent);
            }
        });
        looknotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_notification.setVisibility(View.INVISIBLE);
                Intent intent=new Intent(context,ReceiveFileView.class);
                startActivityForResult(intent,1);
            }
        });
        editText_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editText_search.getText().length()>0)clear.setVisibility(View.VISIBLE);
                else
                    clear.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //ListView滑动事件
        content_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public int scrollstate=0;
            public int scrollpause=0;
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                scrollstate=i;
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                //path_textView.setText("i:"+i+" i1:"+i1+" i2:"+i2);
                previous_i=i;
                Map<String, Object> fileTemp;//哈希表文件
                String filePath;//文件路径和全名
                int k;
                if(scrollstate==AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL||
                        scrollstate==AbsListView.OnScrollListener.SCROLL_STATE_FLING)
                    loadImagePress=true;//如果滑动则允许加载图片
                if(firstOpen){
                    minPosition=i;
                    maxPosition=i;
                    firstOpen=false;
                }
                if(loadImagePress) {
                    if (i < minPosition ) {
                        for (k = i; k < minPosition; k++) {
                            fileTemp = datalist.get(k);
                            filePath = fileTemp.get("path") + "/" + fileTemp.get("text");
                            if (fileOpenWays.fileMinStyle(filePath).equals("photo") && !new File(filePath).isDirectory()) {
                                loadPhoto.loadPhoto(fileTemp,k,datalist,defineHandler);
                            }
                        }
                        minPosition = i;
                        loadImagePress = false;
                    }else if(i+i1>maxPosition){
                        for (k = maxPosition; k < i + i1; k++) {
                            fileTemp = datalist.get(k);
                            filePath = fileTemp.get("path") + "/" + fileTemp.get("text");
                            if (fileOpenWays.fileMinStyle(filePath).equals("photo") && !new File(filePath).isDirectory()) {
                                loadPhoto.loadPhoto(fileTemp,k,datalist,defineHandler);
                            }
                        }
                        maxPosition = k;
                        loadImagePress = false;
                    }
                }
            }
        });
        //ListView长按事件触发
        content_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String,Object> map=(Map<String,Object>)content_listView.getAdapter().getItem(i);
                FP=map.get("path").toString();
                Intent intent=new Intent(context,MselectListView.class);
                intent.putExtra("path",FP);
                intent.putExtra("position",i);
                startActivityForResult(intent,0);
                return true;
            }
        });
        //ListView点击事件
        content_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String FPTemp;
                firstOpen=true;
                Map<String,Object> map=(Map<String,Object>)content_listView.getAdapter().getItem(position);
                FP=map.get("path")+"/"+map.get("text");
                FPTemp=FP;
                if(new File(FP).isDirectory())
                {
                    files = new File(FP).listFiles();
                    if(files!=null){
                        ArrayList<String>nextPath=new ArrayList<String>();
                        ArrayList<String>nextName=new ArrayList<String>();
                        for(File file:files)
                        {
                            nextPath.add(file.getParent());
                            nextName.add(file.getName());
                        }
                        getSimpleAdapterData.getData(nextPath,nextName,datalist);
                        simpleAdapter.notifyDataSetChanged();
                        minPosition=0;
                        maxPosition=0;
                        loadImagePress=true;
                        path_textView.setText(FP.replace("/",">"));
                        nextPath.clear();
                        nextName.clear();
                        content_listView.setSelection(0);//初始位置
                        previous_position.push(previous_i);
                    }
                    else{
                        Toast.makeText(context,"没有权限",Toast.LENGTH_SHORT).show();
                        temp = new File(FP).getParentFile();
                        FP = temp.getAbsolutePath();
                    }
                }
                else
                {
                    final OpenFileMenu openFileMenu=new OpenFileMenu(context,R.style.dialog_menu);
                    openFileMenu.setOnLookOnlineListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(context,SendFileView.class);
                            intent.putExtra("share","unshared");
                            intent.putExtra("file",FPTemp);
                            startActivity(intent);
                            openFileMenu.dismiss();
                        }
                    });
                    openFileMenu.setOnLookOpenListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fileOpenWays.OpenFile(FPTemp,Build.VERSION.SDK_INT,context);
                            openFileMenu.dismiss();
                        }
                    });
                    openFileMenu.setOnLookSharelistener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(context,ShareActivity.class);
                            intent.putExtra("username",username);
                            intent.putExtra("file",FPTemp);
                            startActivity(intent);
                            openFileMenu.dismiss();
                        }
                    });
                    if(fileOpenWays.fileMinStyle(FPTemp).equals("other")){
                        openFileMenu.LookOpen.setVisibility(View.INVISIBLE);
                    }
                    openFileMenu.show();
                    temp = new File(FP).getParentFile();
                    FP = temp.getAbsolutePath();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0){//调用删除activity返回时事件
            if(resultCode==0)
            {
                CreateFileTree();
                showall(show_all_button);
            }
        }else if(requestCode==1){
            CreateFileTree();
            showall(show_all_button);
            new_notification.setVisibility(View.INVISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void clear(View view){
        editText_search.setText("");
    }
    public void search(View view) {
        firstOpen = true;
        String content_search = editText_search.getText().toString();
        if (content_search.equals("")) {
            Toast.makeText(context, "请勿输入空白的关键词", Toast.LENGTH_SHORT).show();
        } else {
            word_search = editText_search.getText().toString();
            searchRuslt();

        }
    }
    public void showall(View view){
        firstOpen=true;
        minPosition=0;
        maxPosition=0;
        files=new File(FP).listFiles();
        ArrayList<String>all_contentPath=new ArrayList<String>();
        ArrayList<String>all_contentName=new ArrayList<String>();
        for(int i=0;i<files.length;i++)
        {
            all_contentPath.add(files[i].getParent());
            all_contentName.add(files[i].getName());
        }
        path_textView.setText(FP.replace("/",">"));
        getSimpleAdapterData.getData(all_contentPath,all_contentName,datalist);
        simpleAdapter.notifyDataSetChanged();

        loadImagePress=true;
        all_contentPath.clear();
        all_contentName.clear();
    }
    public void previous(View view)//上一页方法
    {
        firstOpen=true;
        if(!FP.equals(Environment.getExternalStorageDirectory().toString()))
        {
            temp = new File(FP).getParentFile();
            FP = temp.getPath();
            files = temp.listFiles();
            ArrayList<String>backPath=new ArrayList<String>();
            ArrayList<String>backName=new ArrayList<String>();
            for(File file:files)
            {
                backPath.add(file.getParent());
                backName.add(file.getName());
            }
            getSimpleAdapterData.getData(backPath,backName,datalist);
            simpleAdapter.notifyDataSetChanged();
            minPosition=0;
            maxPosition=0;
            loadImagePress=true;
            backPath.clear();
            backName.clear();
            path_textView.setText(FP.replace("/",">"));
            if(previous_position.size()!=0)
            content_listView.setSelection(previous_position.pop());
        }
        else{
            Toast.makeText(context,"已经是根目录",Toast.LENGTH_SHORT).show();
        }
    }
    //创建文件树
    public FileSystemDate initFileTree(String FileName,FileSystemDate parent)/*参数：文件绝对路径，文件名*/
    {
        File[] files_temp;
        FileSystemDate temp;
        if(new File(FileName).isDirectory())
        {
            files_temp=new File(FileName).listFiles();
            for(File file:files_temp)
            {
                temp=initFileTree(file.getAbsolutePath(),new FileSystemDate(file.getName()));
                parent.add_child(temp);
            }
        }
        else
        {
            return parent;
        }
        return parent;
    }
    //显示搜索结果
    public void searchRuslt() {
        datalist_2.clear();
        LoadingDialog loadingDialog=new LoadingDialog();
        progressDialog=loadingDialog.createLoadingDialog(context,"搜索中");
        progressDialog.show();
        new Thread(){
            public void run(){

                SearchFileSystemData("", fileSystemDate);
                Message m=new Message();
                m.what=0x02;
                defineHandler.sendMessage(m);
            }
        }.start();
    }
    //搜索文件方法
    public int SearchFileSystemData(String filepath,FileSystemDate fileTree){
        String SearchTemp;//临时路径
        if(filepath.equals("")){
            SearchTemp=fileTree.FileName;
        }
        else{
            SearchTemp=filepath+"/"+fileTree.FileName;
        }
        for(int i=0;i<fileTree.getCount();i++){
            if(fileTree.Children.get(i).FileName.contains(word_search)){
                SearchResult.add(fileTree.Children.get(i).FileName);
            }
            getSimpleAdapterData.getSearchData(SearchResult,SearchTemp,datalist_2,word_search);
            SearchResult.clear();
            if(new File(SearchTemp+"/"+fileTree.Children.get(i).FileName).isDirectory()){
                SearchFileSystemData(SearchTemp,fileTree.Children.get(i));
            }
        }
        return 0;
    }
    //手机返回按钮功能
    @Override
    public void onBackPressed() {
        if(!FP.equals(Environment.getExternalStorageDirectory().toString()))
        {
            previous(previos_button);
        }
        else{
            activityStack.FinishAllActivity();
        }
    }
}
