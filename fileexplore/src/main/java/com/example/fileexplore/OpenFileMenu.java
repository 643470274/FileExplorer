package com.example.fileexplore;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

/**
 * Created by 文成 on 2017/5/22.
 */

public class OpenFileMenu extends Dialog {
    public Button LookOnline,LookOpen,LookShare;
    public OpenFileMenu(Context context,int theme) {
        super(context,theme);
        setOpenFileMenu();
    }
    public void setOpenFileMenu(){
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.openfileways_menu, null);
        LookOnline=(Button)mView.findViewById(R.id.LookOnline);
        LookOpen=(Button)mView.findViewById(R.id.LookOpen);
        LookShare=(Button)mView.findViewById(R.id.LookShare);
        super.setContentView(mView);
    }
    public void setOnLookOnlineListener(View.OnClickListener listener){
        LookOnline.setOnClickListener(listener);
    }
    public void setOnLookOpenListener(View.OnClickListener listener){
        LookOpen.setOnClickListener(listener);
    }
    public void setOnLookSharelistener(View.OnClickListener listener){
        LookShare.setOnClickListener(listener);
    }
}
