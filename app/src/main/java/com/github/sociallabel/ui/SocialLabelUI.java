package com.github.sociallabel.ui;

/**
 * Created by jbai on 7/24/2014.
 */
public class SocialLabelUI {

    private static SocialLabelUI instance = null;


    public static synchronized SocialLabelUI getInstance() {
        if (instance == null) {
            synchronized (SocialLabelUI.class) {
                if (instance == null) {
                    instance = new SocialLabelUI();
                }
            }
        }
        return instance;
    }
}
