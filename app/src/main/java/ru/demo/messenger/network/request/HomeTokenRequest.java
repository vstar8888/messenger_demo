package ru.demo.messenger.network.request;

import ru.demo.messenger.network.NetworkConst;

public class HomeTokenRequest extends GetTokenRequest {
    private final String code;
    private final String uid;

    public HomeTokenRequest(String code, String uid) {
        super(NetworkConst.Token.GRANT_TYPE_HOME, NetworkConst.Token.CLIENT_SECRET);
        this.code = code;
        this.uid = uid;
    }
}
