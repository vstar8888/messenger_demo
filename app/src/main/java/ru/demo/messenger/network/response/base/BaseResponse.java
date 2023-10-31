package ru.demo.messenger.network.response.base;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import ru.demo.messenger.network.NetworkConst;


public class BaseResponse {
    @NetworkConst.Status
    String status;

    private String error;
    @SerializedName("error_subtype")
    private String errorSubtype;
    @SerializedName("error_messages")
    private HashMap<String, String> messages;

    /**
     * Get error
     *
     * @return error if isSuccess == false and null - otherwise
     */
    @Nullable
    public ServerError getError() {
        if (isSuccess()) {
            return null;
        } else {
            return new ServerError(error, errorSubtype, messages);
        }
    }

    public final boolean isSuccess() {
        return NetworkConst.Status.STATUS_OK.equalsIgnoreCase(status);
    }


}
