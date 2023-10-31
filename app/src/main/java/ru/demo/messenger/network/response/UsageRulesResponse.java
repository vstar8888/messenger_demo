package ru.demo.messenger.network.response;

import com.google.gson.annotations.SerializedName;

import ru.demo.messenger.network.response.base.BaseResponse;


public class UsageRulesResponse extends BaseResponse {

    @SerializedName("usage_rules_title")
    public String agreementTitle;
    @SerializedName("usage_rules_text")
    public String agreementText;
    @SerializedName("url")
    public String acceptUrl;

}