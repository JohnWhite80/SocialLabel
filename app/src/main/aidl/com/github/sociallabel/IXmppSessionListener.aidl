// IXmppSessionListener.aidl
package com.github.sociallabel;

// Declare any non-default types here with import statements

interface IXmppSessionListener {

    void onLoginResult(int status, String errorMessage);

}
