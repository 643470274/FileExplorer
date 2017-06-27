package com.example.fileexplore;

import java.io.File;
/**
 * Created by 文成 on 2017/5/15.
 */

public class DeleteFile{
    public static void deleteFile(File file){
        if(file.isDirectory()){
            File[] childFile=file.listFiles();
            if(childFile==null||childFile.length==0)
            {
                file.delete();
                return;
            }
            for(File filetemp:childFile){
                deleteFile(filetemp);
            }
            file.delete();
        }
        else{
            file.delete();
        }
    }
}
