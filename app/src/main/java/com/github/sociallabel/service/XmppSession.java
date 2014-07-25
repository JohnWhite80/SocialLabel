package com.github.sociallabel.service;

import android.os.RemoteException;
import android.util.Log;

import com.github.sociallabel.IXmppMucListener;
import com.github.sociallabel.IXmppMucManager;
import com.github.sociallabel.IXmppSessionListener;
import com.github.sociallabel.IXmppSessionManager;
import com.github.sociallabel.Constant;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DelayInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jbai on 7/2/2014.
 */
public class XmppSession extends IXmppSessionManager.Stub {

    private List<IXmppSessionListener> listeners = new ArrayList<IXmppSessionListener>();
    private String serveraddress = Constant.SERVER_ADDRESS;
    private int serverPort = Constant.SERVER_PORT;
    private Connection connection = null;
    private String username;
    private String password;
    private XmppMucManager xmppMucManager = new XmppMucManager();

    public XmppMucManager getIXmppMucManager() {
        return xmppMucManager;
    }

    @Override
    public void registerListener(IXmppSessionListener listener) throws RemoteException {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(IXmppSessionListener listener) throws RemoteException {
        listeners.remove(listener);
    }

    @Override
    public boolean isConnected() throws RemoteException {
        if (connection != null) {
            return connection.isConnected();
        }
        return false;
    }

    @Override
    public synchronized void connect(String username, String password) throws RemoteException {
        if (isConnected()) {
            notifyLogin(1, "already connected");
            return;
        }
        ConnectionConfiguration config = new ConnectionConfiguration(serveraddress, serverPort);
        config.setCompressionEnabled(true);
        config.setSASLAuthenticationEnabled(true);
        try {
            connection = new XMPPConnection(config);
            connection.connect();
            connection.login(username, password, "smack");

            assert connection.isSecureConnection();
            notifyLogin(0, "login succeeded");
        } catch (XMPPException e) {
            notifyLogin(-1, "login failed, reason : " + e.getMessage());
        }
    }

    private void notifyLogin(int result, String message) throws RemoteException {
        if (listeners != null) {
            for (IXmppSessionListener listener : listeners) {
                listener.onLoginResult(result, message);
            }
        }
    }

    @Override
    public synchronized void disconnect() throws RemoteException {
        if (isConnected()) {
            connection.disconnect();
            connection = null;
        }
    }

    private class XmppMucManager extends IXmppMucManager.Stub {

        private MultiUserChat muc = null;
        private String roomName = null;
        private List<String> occupants = null;
        private IXmppMucListener listener;

        @Override
        public void registerListener(IXmppMucListener listener) throws RemoteException {
            this.listener = listener;
        }

        @Override
        public void unregisterListener(IXmppMucListener listener) throws RemoteException {
            this.listener = null;
        }

        @Override
        public String getRoom() throws RemoteException {
            if (XmppSession.this.isConnected() && muc != null) {
                return roomName;
            }
            return null;
        }

        @Override
        public List<String> getOccupants() throws RemoteException {
            if (XmppSession.this.isConnected() && muc != null) {
                List<String> result = new ArrayList<String>();
                Iterator<String> it = occupants.iterator();
                while (it.hasNext()) {
                    String i = it.next();
                    String nike = i.replaceAll(getRoom() + "/", "");
                    result.add(nike);
                }
                return result;
            }
            return Collections.EMPTY_LIST;
        }

        @Override
        public void joinMuc(String roomName, String nickName) throws RemoteException {
            if (XmppSession.this.isConnected()) {
                try {
                    muc = new MultiUserChat(connection, roomName + "@muc." + serveraddress);
                    muc.join(nickName);
                    Log.i("social-lable", "joined conversation!");
                    occupants = new ArrayList<String>();
                    Iterator<String> it = muc.getOccupants();
                    while (it.hasNext()) {
                        String s = it.next();
                        occupants.add(s);
                    }
                    this.roomName = muc.getRoom();
                    muc.addUserStatusListener(new DefaultUserStatusListener() {
                        @Override
                        public void kicked(String actor, String reason) {
                            if (listener != null) {
                                try {
                                    listener.onMucKicked(null, actor, reason);
                                } catch (RemoteException e) {
                                    Log.e("social-lable", "failed to handle kicked", e);
                                }
                            }
                        }
                    });
                    muc.addParticipantStatusListener(new DefaultParticipantStatusListener() {
                        @Override
                        public void kicked(String participant, String actor, String reason) {
                            if (listener != null) {
                                try {
                                    occupants.remove(participant);
                                    listener.onMucKicked(participant, actor, reason);
                                } catch (RemoteException e) {
                                    Log.e("social-lable", "failed to handle kicked", e);
                                }
                            }
                        }

                        @Override
                        public void joined(String participant) {
                            if (listener != null) {
                                try {
                                    occupants.add(participant);
                                    listener.onMucJoined(participant);
                                } catch (RemoteException e) {
                                    Log.e("social-lable", "failed to handle joined", e);
                                }
                            }
                        }

                        @Override
                        public void left(String participant) {
                            if (listener != null) {
                                try {
                                    listener.onMucLeft(participant);
                                } catch (RemoteException e) {
                                    Log.e("social-lable", "failed to handle left", e);
                                }
                            }
                        }
                    });
                    muc.addMessageListener(new PacketListener() {
                        @Override
                        public void processPacket(Packet packet) {
                            if (packet instanceof Message) {
                                Message msg = (Message) packet;
                                if (listener != null) {
                                    Log.i("social-lable", "Received message : " + msg.getBody());
                                    String from = msg.getFrom().replaceAll(muc.getRoom() + "/", "");
                                    DelayInformation inf = (DelayInformation) msg.getExtension("x", "jabber:x:delay");
                                    Date sentDate;
                                    if (inf != null) {
                                        sentDate = inf.getStamp();
                                    } else {
                                        sentDate = new Date();
                                    }
                                    try {
                                        listener.onMucMessage(from, msg.getBody(), sentDate.toString());
                                    } catch (RemoteException e) {
                                        Log.e("social-lable", "failed to handle message", e);
                                    }
                                }

                            }
                        }
                    });
                } catch (XMPPException e) {
                    Log.e("social-lable", "failed to join muc", e);
                    throw new RemoteException(e.getMessage());
                }
            }
        }

        @Override
        public void sendMucMessage(String message) throws RemoteException {
            if (isConnected() && muc != null) {
                try {
                    muc.sendMessage(message);
                } catch (XMPPException e) {
                    Log.e("social-lable", "failed to send muc message", e);
                    throw new RemoteException(e.getMessage());
                }
            }
        }

        @Override
        public void kickParticipant(String nickname, String reason) throws RemoteException {
            if (isConnected() && muc != null) {
                try {
                    muc.kickParticipant(nickname, reason);
                } catch (XMPPException e) {
                    Log.e("social-lable", "failed to kickParticipant", e);
                    throw new RemoteException(e.getMessage());
                }
            }
        }

        @Override
        public void leaveMuc() throws RemoteException {
            if (isConnected() && muc != null) {
                muc.leave();
                muc = null;
                roomName = null;
                occupants = null;
            }
        }
    }
}
