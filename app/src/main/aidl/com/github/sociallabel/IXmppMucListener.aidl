// IXmppMucListener.aidl
package com.github.sociallabel;

// Declare any non-default types here with import statements

interface IXmppMucListener {

    void onMucMessage(String from, String message, String date);

    void onMucKicked(String participant, String actor, String reason);

    void onMucLeft(String participant);

    void onMucJoined(String participant);
}
