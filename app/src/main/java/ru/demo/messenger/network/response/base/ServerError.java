package ru.demo.messenger.network.response.base;


import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.Map;

import ru.demo.messenger.network.NetworkConst;


public final class ServerError {
    private String errorType;
    private String errorSubType;
    private Map<String, String> messages;

    public ServerError(String errorType, String defaultMessage) {
        this(errorType, null, Collections.singletonMap(NetworkConst.ErrorCodes.DEFAULT_KEY, defaultMessage));
    }

    public ServerError(String errorType, String errorSubType, Map<String, String> messages) {
        this.errorType = errorType;
        this.errorSubType = errorSubType;
        this.messages = messages;
    }

    public String getErrorType() {
        return errorType;
    }

    @Nullable
    public String getErrorSubType() {
        return errorSubType;
    }

    public String getMessage() {
        String defaultMessage = getMessage(NetworkConst.ErrorCodes.DEFAULT_KEY);
        if (!TextUtils.isEmpty(defaultMessage)) {
            return defaultMessage;
        } else if (messages.size() == 1) {
            for (Map.Entry<String, String> entry : messages.entrySet()) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getMessage(String key) {
        return messages.get(key);
    }


}
