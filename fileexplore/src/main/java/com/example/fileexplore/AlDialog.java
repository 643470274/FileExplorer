package com.example.fileexplore;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by 文成 on 2017/5/16.
 */

public class AlDialog extends Dialog {
    private TextView DialogTitle,DialogMessage;
    private Button PositiveButton,NegativeButton;
    public AlDialog(Context context) {
        super(context);
        setAlertDialog();
    }
    private void setAlertDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog, null);
        DialogTitle = (TextView) mView.findViewById(R.id.Dialog_Title);
        DialogMessage=(TextView)mView.findViewById(R.id.Dialog_Message);
        PositiveButton = (Button) mView.findViewById(R.id.positive_button);
        NegativeButton = (Button) mView.findViewById(R.id.negative_button);
        super.setContentView(mView);
    }
    /**
     * 确定键监听器
     * @param listener
     */

    public void setOnPositiveListener(View.OnClickListener listener){
        PositiveButton.setOnClickListener(listener);
    }
    /**
     * 取消键监听器
     * @param listener
     */
    public void setOnNegativeListener(View.OnClickListener listener){
        NegativeButton.setOnClickListener(listener);
    }

}
