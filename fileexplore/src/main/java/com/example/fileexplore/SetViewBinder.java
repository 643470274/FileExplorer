package com.example.fileexplore;

import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Created by 文成 on 2017/4/15.
 */

public class SetViewBinder {//适配SimpleAdapter
    public SimpleAdapter setviewbinder(SimpleAdapter simpleAdapter){
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation)
            {
                if(view instanceof ImageView && data instanceof Bitmap)
                {
                    ImageView iv = (ImageView) view;
                    iv.setImageBitmap((Bitmap) data);
                    return true;
                }else if(view instanceof TextView && data instanceof SpannableStringBuilder){
                    TextView tv=(TextView) view;
                    tv.setText((SpannableStringBuilder)data);
                    return true;
                }else
                    return false;
            }
        });
        return simpleAdapter;
    }
}
