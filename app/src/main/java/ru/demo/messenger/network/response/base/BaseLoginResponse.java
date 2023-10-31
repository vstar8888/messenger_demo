package ru.demo.messenger.network.response.base;


import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class BaseLoginResponse extends BaseResponse {

    private static final String VALUE_SIGNUP_USER         = "signup_user";
    private static final String VALUE_START_LOGIN_USER    = "start_login_user";
    private static final String VALUE_GET_TOKEN           = "get_token";
    private static final String VALUE_CREATE_COMPANY      = "create_company";
    private static final String VALUE_CHOSE_COMPANY       = "chose_company";
    private static final String VALUE_GO_TO_BROWSER       = "goto_browser";
    private static final String VALUE_GO_TO_ACCEPT_POLICY = "accept_policy";

    @SerializedName("next_step")
    public String nextStep;

    @SerializedName("url")
    public String url;

    @SerializedName("get_new_app_token")
    public boolean getNewAppToken;

    @SerializedName("get_new_app_token_url")
    @Nullable
    public String getNewAppTokenUrl;

    public NextStepType getNextStep() {
        return NextStepType.parse(nextStep);
    }

    public enum NextStepType {
        SIGNUP_USER(VALUE_SIGNUP_USER),
        START_LOGIN_USER(VALUE_START_LOGIN_USER),
        GET_TOKEN(VALUE_GET_TOKEN),
        CREATE_COMPANY(VALUE_CREATE_COMPANY),
        CHOSE_COMPANY(VALUE_CHOSE_COMPANY),
        GO_TO_BROWSER(VALUE_GO_TO_BROWSER),
        GO_TO_ACCEPT_POLICY(VALUE_GO_TO_ACCEPT_POLICY)
        ;

        String value;

        NextStepType(String value) {
            this.value = value;
        }

        @Nullable
        public static NextStepType parse(@Nullable String value) {
            for(NextStepType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }

}
