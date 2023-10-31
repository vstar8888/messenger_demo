package ru.demo.messenger.network.request;


import com.google.gson.annotations.SerializedName;

public class CompleteConfirmPhoneRequest {
    @SerializedName("sms_confirmation_id")
    String smsConfimationId;
    @SerializedName("sms_confirmation_code")
    String smsCode;

    public CompleteConfirmPhoneRequest(String smsConfimationId, String smsCode) {
        this.smsConfimationId = smsConfimationId;
        this.smsCode = smsCode;
    }
}
