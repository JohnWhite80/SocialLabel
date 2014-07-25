// IXmppSessionManager.aidl
package com.github.sociallabel;

// Declare any non-default types here with import statements
import com.github.sociallabel.IXmppSessionListener;

interface IXmppSessionManager {

    void registerListener(IXmppSessionListener listener);

    void unregisterListener(IXmppSessionListener listener);

    void connect(String username, String password);

    boolean isConnected();

    void disconnect();
}
