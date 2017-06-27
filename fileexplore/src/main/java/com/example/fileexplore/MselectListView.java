package com.example.fileexplore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 文成 on 2017/5/14.
 */

public class MselectListView extends BaseActivity {
    private int minPosition=0;//异步加载图片最小位置记录
    private int maxPosition=0;//异步加载图片最大位置记录
    private boolean loadImagePress=true;//允许加载图片
    private boolean firstOpen=true;//第一次打开页面
    private TextView title;
    private ListView select;
    private Button go_back,button_delete_bottom,button_all_select_bottom,button_reverse_select_bottom;
    private View layout_bottom;
    private FileOpenWays fileOpenWays=new FileOpenWays();//文件类型类
    private LoadPhoto loadPhoto=new LoadPhoto();//异步加载图
    private Context context=this;//全局（本类）
    private SimpleAdapter simpleAdapter;//适配器
    private List<HashMap<String,Object>> datalist;//数据源
    private GetSimpleAdapterData getSimpleAdapterData=new GetSimpleAdapterData();//获取适配器数据源
    private SetViewBinder setViewBinder=new SetViewBinder();//适配器数据适配
    private String FP;//地址
    private int position;
    private File[] files;
    private SetStatusBar setStatusBar=new SetStatusBar();//状态栏
    final Handler defineHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x00:
                    simpleAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Translucent translucent=new Translucent();
        translucent.setStatusTranslucent(getWindow());
        setContentView(R.layout.mselectlistview);

        title=(TextView)findViewById(R.id.selsect_title);
        select=(ListView)findViewById(R.id.select);
        go_back=(Button)findViewById(R.id.button_back);
        button_all_select_bottom=(Button)findViewById(R.id.button_all_select_bottom);
        button_reverse_select_bottom=(Button)findViewById(R.id.button_reverse_select_bottom);
        button_delete_bottom=(Button)findViewById(R.id.button_delete_bottom);
        layout_bottom=findViewById(R.id.layout_bottom);

        Intent intent=getIntent();//Intent链接
        ArrayList<String> genPath = new ArrayList<String>();
        ArrayList<String> genName = new ArrayList<String>();
        ArrayList<Boolean> check=new ArrayList<Boolean>();

        if(Build.VERSION.SDK_INT<21) {//判断Android版本信息，选择透明状态栏方法。
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//5.0之前透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        //font_white
        setStatusBar.MIUISetStatusBarLightMode(this.getWindow(),false);
        setStatusBar.FlymeSetStatusBarLightMode(this.getWindow(),false);
        FP=intent.getStringExtra("path");
        position=intent.getIntExtra("position",0);
        title.setText(FP);
        files = new File(FP).listFiles();//遍历根目录
        int i=0;
        for (File file : files)//将根目录名字加入容器
        {
            genPath.add(file.getParent());
            genName.add(file.getName());
            if(i==position)
            check.add(true);
            else
            check.add(false);
            i++;
        }
        datalist = new ArrayList<HashMap<String, Object>>();
        getSimpleAdapterData.getSelectData(genPath,genName,check,datalist);
        simpleAdapter = new SimpleAdapter(this, datalist, R.layout.item_select, new String[]{"pic", "text","path","check"}, new int[]{R.id.pic, R.id.text,0,R.id.checkBox});
         /*
        调整simpleAdapter内View数据源
        */
        simpleAdapter=setViewBinder.setviewbinder(simpleAdapter);
        select.setAdapter(simpleAdapter);
        select.setSelection(position);
        button_lis();
        Animation_bottom();
    }
    public void Animation_bottom(){
        layout_bottom.setVisibility(View.VISIBLE);
        final Animation animation1= AnimationUtils.loadAnimation(context,R.anim.button_bottom_anim);
        final Animation animation2= AnimationUtils.loadAnimation(context,R.anim.button_bottom_anim);
        final Animation animation3= AnimationUtils.loadAnimation(context,R.anim.button_bottom_anim);
        final Animation animation0=AnimationUtils.loadAnimation(context,R.anim.init_button_bottom_anim);
        layout_bottom.startAnimation(animation0);
        button_delete_bottom.postDelayed(new Runnable() {
            @Override
            public void run() {
                button_delete_bottom.startAnimation(animation1);
            }
        },0);
        button_all_select_bottom.postDelayed(new Runnable() {
            @Override
            public void run() {
                button_all_select_bottom.startAnimation(animation2);
            }
        },100);
        button_reverse_select_bottom.postDelayed(new Runnable() {
            @Override
            public void run() {
                button_reverse_select_bottom.startAnimation(animation3);
            }
        },200);
    }

    @Override
    public void onBackPressed() {//back
        Intent result=new Intent();
        result.putExtra("result","删除成功");
        setResult(0,result);
        MselectListView.this.finish();
    }
    public void button_lis(){
        //back
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent result=new Intent();
                result.putExtra("result","删除成功");
                setResult(0,result);
                MselectListView.this.finish();
            }
        });
        //ListView滑动事件
        select.setOnScrollListener(new AbsListView.OnScrollListener() {
            public int scrollstate=0;
            public int scrollpause=0;
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                scrollstate=i;
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

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
        //listview单击事件
        select.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String,Object>temp=new HashMap<>();
                boolean check= (boolean) datalist.get(i).get("check");
                temp.put("pic",datalist.get(i).get("pic"));
                temp.put("text",datalist.get(i).get("text"));
                temp.put("path",datalist.get(i).get("path"));
                if(check)
                    check=false;
                else
                    check=true;
                temp.put("check",check);
                datalist.set(i,temp);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        //全选按钮事件
        button_all_select_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i=0;
                for(i=0;i<datalist.size();i++){
                    HashMap<String,Object>temp=new HashMap<>();
                    if(!(boolean)datalist.get(i).get("check")){
                        temp.put("pic",datalist.get(i).get("pic"));
                        temp.put("text",datalist.get(i).get("text"));
                        temp.put("path",datalist.get(i).get("path"));
                        temp.put("check",true);
                        datalist.set(i,temp);
                    }
                }
                simpleAdapter.notifyDataSetChanged();
            }
        });
        //反选按钮事件
        button_reverse_select_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i=0;
                for(i=0;i<datalist.size();i++){
                    HashMap<String,Object>temp=new HashMap<>();
                    if(!(boolean)datalist.get(i).get("check")){
                        temp.put("pic",datalist.get(i).get("pic"));
                        temp.put("text",datalist.get(i).get("text"));
                        temp.put("path",datalist.get(i).get("path"));
                        temp.put("check",true);
                    }else {
                        temp.put("pic",datalist.get(i).get("pic"));
                        temp.put("text",datalist.get(i).get("text"));
                        temp.put("path",datalist.get(i).get("path"));
                        temp.put("check",false);
                    }
                    datalist.set(i,temp);
                }
                simpleAdapter.notifyDataSetChanged();
            }
        });
        //删除按钮事件
        button_delete_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlDialog alertDialog=new AlDialog(MselectListView.this);
                alertDialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i=0;
                        for(i=0;i<datalist.size();i++){
                            if((boolean)datalist.get(i).get("check")){
                                File file=new File(datalist.get(i).get("path")+"/"+datalist.get(i).get("text"));
                                DeleteFile.deleteFile(file);
                                datalist.remove(i);
                                i--;
                                simpleAdapter.notifyDataSetChanged();
                                minPosition=0;//异步加载图片最小位置记录
                                maxPosition=0;//异步加载图片最大位置记录
                                loadImagePress=true;//允许加载图片
                                firstOpen=true;//第一次打开页面
                            }
                        }
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }
}
