package com.github.sociallabel.ui;

import android.app.Application;
import android.content.Intent;

import com.github.sociallabel.ServiceAction;

import org.jivesoftware.smack.SmackAndroid;

/**
 * Created by jbai on 7/24/2014.
 */
public class SocialLabelApplication extends Application {

    private static SocialLabelApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Intent serviceIntent = new Intent(ServiceAction.ACTION_SERVICE);
        startService(serviceIntent);
        SocialLabelUI.getInstance();
        SmackAndroid.init(getApplicationContext());
    }

    public static SocialLabelApplication getInstance(){
        return instance;
    }
}
