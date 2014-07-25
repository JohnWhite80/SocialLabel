package com.github.sociallabel.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.github.sociallabel.ServiceAction;

public class SocialLabelService extends Service {

    private SocialLabel socialLabel = null;
    @Override
    public void onCreate() {
        super.onCreate();
        socialLabel = SocialLabel.getInstance();
        socialLabel.start(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getAction().equals(ServiceAction.ACTION_XMPP_SERVICE)) {
            return socialLabel.getXmppService();
        } else if(intent.getAction().equals(ServiceAction.ACTION_XMPP_MUC_SERVICE)) {
            return socialLabel.getXmppMucManager();
        }
        return null;
    }
}
