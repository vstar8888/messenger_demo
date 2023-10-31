package ru.demo.messenger.chats.single.info;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.SOURCE)
@StringDef({InfoReadStatus.UNREAD, InfoReadStatus.ISREAD})
public @interface InfoReadStatus {
    String UNREAD = "UnRead";
    String ISREAD = "IsRead";
}