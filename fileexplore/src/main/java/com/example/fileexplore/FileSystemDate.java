package com.example.fileexplore;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by w6434 on 2017/3/17.
 */

public class FileSystemDate{//文件树
    protected String FileName;
    protected ArrayList<FileSystemDate> Children;
    public FileSystemDate(String fileName)
    {
        FileName=fileName;
        if(Children==null)
        Children=new ArrayList<FileSystemDate>();
    }
    public void add_child(FileSystemDate child)
    {
        if(Children==null)
            Children=new ArrayList<FileSystemDate>();
        Children.add(child);
    }
    public String getFileName(int i){
        return Children.get(i).FileName;
    }
    public int getCount()
    {
        return Children.size();
    }
}
