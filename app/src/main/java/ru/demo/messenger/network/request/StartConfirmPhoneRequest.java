package ru.demo.messenger.network.request;


import com.google.gson.annotations.SerializedName;

import ru.demo.messenger.utils.DeviceUtils;

public class StartConfirmPhoneRequest {

    @SerializedName("phone_number")
    String phone;
    @SerializedName("user_ip_address")
    String ipAddress;
    @SerializedName("user_agent")
    String userAgent;

    public StartConfirmPhoneRequest(String phone) {
        this.phone = phone;
        this.ipAddress = DeviceUtils.getIPAddress(true);
        this.userAgent = DeviceUtils.getPhoneModelAndVersion();
    }
}
