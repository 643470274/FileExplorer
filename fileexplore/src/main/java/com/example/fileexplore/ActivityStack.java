package com.example.fileexplore;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by Monkey D Luffy on 2017/6/7.
 */

public class ActivityStack {
    private Stack<Activity> activities;
    private static ActivityStack activityStack=new ActivityStack();
    public ActivityStack(){
        activities=new Stack<Activity>();
    }
    public static ActivityStack getActivityStack(){
        return activityStack;
    }
    public void PushActivity(Activity activity){
        activities.push(activity);
    }
    public Activity PopActivity(){
        return activities.pop();
    }
    public void FinishAllActivity(){
        while (!activities.isEmpty()){
            activities.pop().finish();
        }
    }
}
