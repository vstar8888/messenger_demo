package ru.demo.messenger.network.request;


import ru.demo.messenger.network.NetworkConst;


public class GetTokenRequest {
    private final String grant_type;
    private final String client_secret;

    public GetTokenRequest() {
        this(NetworkConst.Token.GRANT_TYPE_CLIENT, NetworkConst.Token.CLIENT_SECRET);
    }

    public GetTokenRequest(String grant_type, String client_secret) {
        this.grant_type = grant_type;
        this.client_secret = client_secret;
    }
}
