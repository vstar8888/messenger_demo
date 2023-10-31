package ru.demo.messenger.network.request;

public class NotificationToken {
    private final String app_name = "Messenger";
    private final String registration_id;

    public NotificationToken(String token) {
        this.registration_id = token;
    }
}
