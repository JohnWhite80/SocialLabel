package com.github.sociallabel.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jbai on 7/2/2014.
 */
public class SocialLabel {

    private static SocialLabel instance = new SocialLabel();
    private SocialLabelService socialLabelService;
    private XmppSession xmppSession;

    public static void autoStartReceived(Context context) {
        Intent i = new Intent();
        i.setClass(context, SocialLabelService.class);
        context.startService(i);
    }

    public static SocialLabel getInstance() {
        return instance;
    }

    public void start(SocialLabelService socialLabelService) {
        this.socialLabelService = socialLabelService;
        xmppSession = new XmppSession();
    }

    public IBinder getXmppService() {
        return xmppSession;
    }

    public IBinder getXmppMucManager(){
        return xmppSession.getIXmppMucManager();
    }
}
