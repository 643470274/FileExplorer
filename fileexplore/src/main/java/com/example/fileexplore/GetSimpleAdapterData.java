package com.example.fileexplore;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 文成 on 2017/4/15.
 */

public class GetSimpleAdapterData {//获取simpleadapter数据
    private FileOpenWays fileOpenWays=new FileOpenWays();
    protected void getWebFileData(ArrayList<String> filepath, ArrayList<String> friends,List<HashMap<String,Object>> datalist){
        for(int i=0;i<filepath.size();i++){
            HashMap<String,Object> map=new HashMap<>();
            if(fileOpenWays.fileMinStyle(filepath.get(i)).equals("other")){
                map.put("pic",R.mipmap.fileother);
                map.put("friend",friends.get(i));
                map.put("path",filepath.get(i));
            }else if(fileOpenWays.fileMinStyle(filepath.get(i)).equals("music")){
                map.put("pic",R.mipmap.filemusic);
                map.put("friend",friends.get(i));
                map.put("path",filepath.get(i));
            }else if(fileOpenWays.fileMinStyle(filepath.get(i)).equals("photo")){
                map.put("pic",R.mipmap.filephoto);
                map.put("friend",friends.get(i));
                map.put("path",filepath.get(i));
            }
            datalist.add(map);
        }
    }
    protected void getSearchData(ArrayList<String> gen, String filepath,List<HashMap<String,Object>> datalist_2,String word_search){
        for(int i=0;i<gen.size();i++){
            HashMap<String,Object> map=new HashMap<>();
            SpannableStringBuilder builder = new SpannableStringBuilder(gen.get(i));
            ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor("#ff4081"));//Color.parseColor("#f45b5b")
            builder.setSpan(redSpan, gen.get(i).indexOf(word_search), gen.get(i).indexOf(word_search)+word_search.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(new File(filepath+"/"+gen.get(i)).isDirectory()){
                map.put("pic",R.mipmap.folder);
                map.put("text",builder);//gen.get(i)
                map.put("path",filepath);
                datalist_2.add(map);
            }
            else{
                if(fileOpenWays.fileMinStyle(gen.get(i)).equals("other")){
                    map.put("pic",R.mipmap.fileother);
                    map.put("text",builder);//gen.get(i)
                    map.put("path",filepath);
                }else if(fileOpenWays.fileMinStyle(gen.get(i)).equals("music")){
                    map.put("pic",R.mipmap.filemusic);
                    map.put("text",builder);//gen.get(i)
                    map.put("path",filepath);
                }else if(fileOpenWays.fileMinStyle(gen.get(i)).equals("photo")){
                    map.put("pic",R.mipmap.filephoto);
                    map.put("text",builder);//gen.get(i)
                    map.put("path",filepath);
                }
                datalist_2.add(map);
            }
        }
    }
    protected List<HashMap<String,Object>> getData(ArrayList<String>gen, ArrayList<String>genName,List<HashMap<String,Object>> datalist)
    {   datalist.clear();
        Collections.sort(genName, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        for(int i=0;i<gen.size();i++)
        {
            HashMap<String,Object>map=new HashMap<>();
            if(new File(gen.get(i)+"/"+genName.get(i)).isDirectory())
            {
                map.put("pic",R.mipmap.folder);
                map.put("text",genName.get(i));//
                map.put("path",gen.get(i));
                datalist.add(map);
            }
        }
        for(int i=0;i<gen.size();i++)
        {
            HashMap<String,Object>map=new HashMap<>();
            if(!new File(gen.get(i)+"/"+genName.get(i)).isDirectory())
            {
                if(fileOpenWays.fileMinStyle(genName.get(i)).equals("other")){
                    map.put("pic",R.mipmap.fileother);
                    map.put("text",genName.get(i));
                    map.put("path",gen.get(i));
                }else if(fileOpenWays.fileMinStyle(genName.get(i)).equals("music")){
                    map.put("pic",R.mipmap.filemusic);
                    map.put("text",genName.get(i));
                    map.put("path",gen.get(i));
                }else if(fileOpenWays.fileMinStyle(genName.get(i)).equals("photo")){
                    map.put("pic",R.mipmap.filephoto);
                    map.put("text",genName.get(i));
                    map.put("path",gen.get(i));
                }
                datalist.add(map);
            }
        }
        return datalist;
    }
    protected List<HashMap<String,Object>> getSelectData(ArrayList<String>gen, ArrayList<String>genName, ArrayList<Boolean>check, List<HashMap<String,Object>> datalist){
        HashMap<String,Object>temp=new HashMap<>();
        datalist.clear();
        Collections.sort(genName, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        for(int i=0;i<gen.size();i++)
        {
            HashMap<String,Object>map=new HashMap<>();
            if(new File(gen.get(i)+"/"+genName.get(i)).isDirectory())
            {
                map.put("pic",R.mipmap.folder);
                map.put("text",genName.get(i));//
                map.put("path",gen.get(i));
//              map.put("check",check.get(i));
                datalist.add(map);
            }
        }
        for(int i=0;i<gen.size();i++)
        {
            HashMap<String,Object>map=new HashMap<>();
            map.clear();
            if(!new File(gen.get(i)+"/"+genName.get(i)).isDirectory())
            {
                if(fileOpenWays.fileMinStyle(genName.get(i)).equals("other")){
                    map.put("pic",R.mipmap.fileother);
                    map.put("text",genName.get(i));
                    map.put("path",gen.get(i));
                }else if(fileOpenWays.fileMinStyle(genName.get(i)).equals("music")){
                    map.put("pic",R.mipmap.filemusic);
                    map.put("text",genName.get(i));
                    map.put("path",gen.get(i));
                }else if(fileOpenWays.fileMinStyle(genName.get(i)).equals("photo")){
                    map.put("pic",R.mipmap.filephoto);
                    map.put("text",genName.get(i));
                    map.put("path",gen.get(i));
                }
                datalist.add(map);
            }
        }
        for(int i=0;i<gen.size();i++)
        {
            HashMap<String,Object>map=new HashMap<>();
            temp=datalist.get(i);
            map.put("pic",temp.get("pic"));
            map.put("text",temp.get("text"));
            map.put("path",temp.get("path"));
            map.put("check",check.get(i));
            datalist.set(i,map);
        }
        return datalist;
    }
}
