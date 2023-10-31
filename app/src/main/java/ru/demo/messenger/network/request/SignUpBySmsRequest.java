package ru.demo.messenger.network.request;


import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import ru.demo.messenger.utils.DeviceUtils;

public class SignUpBySmsRequest {

    @SerializedName("last_name")
    private String lastName;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("phone_number")
    private String phoneNumber;
    @SerializedName("ip_address")
    private String ipAddress;
    @SerializedName("user_agent")
    private String userAgent;
    private String position;
    private String subdivision;
    @SerializedName("policy_accepted")
    private boolean policyAccepted;


    public SignUpBySmsRequest(String phoneNumber, String firstName, String lastName,
                              @Nullable String position, @Nullable String subdivision, boolean policyAccepted) {
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.ipAddress = DeviceUtils.getIPAddress(true);
        this.userAgent = DeviceUtils.getPhoneModelAndVersion();
        this.position = position;
        this.subdivision = subdivision;
        this.policyAccepted = policyAccepted;
    }

}
