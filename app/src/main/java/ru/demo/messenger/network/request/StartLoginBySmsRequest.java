package ru.demo.messenger.network.request;


import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import ru.demo.messenger.utils.DeviceUtils;

public class StartLoginBySmsRequest {
    @SerializedName("phone_number")
    String phone;
    @SerializedName("ip_address")
    String ipAddress;
    @SerializedName("user_agent")
    String userAgent;
    @SerializedName("company_host")
    String companyHost;

    public StartLoginBySmsRequest(String phone, @Nullable String companyHost) {
        this.phone = phone;
        this.companyHost = companyHost;
        this.ipAddress = DeviceUtils.getIPAddress(true);
        this.userAgent = DeviceUtils.getPhoneModelAndVersion();
    }


}
