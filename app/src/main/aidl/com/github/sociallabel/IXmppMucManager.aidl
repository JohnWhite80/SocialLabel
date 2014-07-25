// IXmppMucManager.aidl
package com.github.sociallabel;

// Declare any non-default types here with import statements
import com.github.sociallabel.IXmppMucListener;

interface IXmppMucManager {

    void registerListener(IXmppMucListener listener);

    void unregisterListener(IXmppMucListener listener);

    void joinMuc(String roomName, String nickName);

    String getRoom();

    void sendMucMessage(String message);

    void kickParticipant(String nickname, String reason);

    void leaveMuc();

    List<String> getOccupants();
}
