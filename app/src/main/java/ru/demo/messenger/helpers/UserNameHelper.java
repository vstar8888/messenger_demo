package ru.demo.messenger.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserNameHelper {

    @NonNull
    public static String getInitials(@Nullable String fullName) {
        if (fullName == null) {
            return "";
        }
        String[] words = fullName.split("\\s+");
        String initials;
        if (words.length == 0) {
            return "";
        } else if (words.length > 1) {
            initials = String.valueOf(new char[]{
                    Character.toUpperCase(words[0].charAt(0)),
                    Character.toUpperCase(words[1].charAt(0))
            });
        } else {
            initials = String.valueOf(Character.toUpperCase(words[0].charAt(0)));
        }
        return initials;
    }

}
