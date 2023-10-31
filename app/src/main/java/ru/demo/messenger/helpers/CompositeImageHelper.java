package ru.demo.messenger.helpers;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ru.demo.messenger.R;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.people.TextDrawable;

public class CompositeImageHelper {

    private final ColorGenerator colorGenerator;

    public CompositeImageHelper(@NonNull ColorGenerator colorGenerator) {
        this.colorGenerator = colorGenerator;
    }

    public void showChatImage(@NonNull CompositeImageView imageView,
                              @NonNull ChatModel chat, long selfUserId) {
        if (TextUtils.isEmpty(chat.getPhoto())) {
            final List<UserModel> users = usersWithoutSelf(chat.getUsers(), selfUserId);
            final boolean roundPlaceHolder = users.size() == 1;
            if (users.isEmpty()) {
                imageView.setPlaceholderCreator((position, width, height) ->
                        ContextCompat.getDrawable(imageView.getContext(), R.drawable.ic_placeholder_group)
                );
                imageView.setImageUrl(null);
            } else {
                imageView.setPlaceholderCreator((position, width, height) -> {
                    final UserModel user = users.get(position);
                    return new TextDrawable(width, height,
                            colorGenerator.from(user),
                            UserNameHelper.getInitials(user.getFullName()),
                            roundPlaceHolder
                    );
                });
                imageView.setImageUrls(photoUrlsFromUsers(users));
            }
        } else {
            imageView.setPlaceholderCreator(null);
            imageView.setImageUrl(chat.getPhoto());
        }
    }

    public void showUserImage(@NonNull CompositeImageView imageView,
                              @NonNull UserModel user) {
        imageView.setPlaceholderCreator((position, width, height) ->
                new TextDrawable(width, height,
                        colorGenerator.from(user),
                        UserNameHelper.getInitials(user.getFullName()),
                        true
                )
        );
        imageView.setImageUrl(user.getThumbnailX2());
    }

    @NonNull
    private List<UserModel> usersWithoutSelf(@NonNull List<UserModel> users, long selfUserId) {
        final List<UserModel> filteredUsers = new ArrayList<>(users.size());
        for (int i = 0; i < users.size(); i++) {
            final UserModel user = users.get(i);
            if (user.getId() == selfUserId) {
                continue;
            }
            filteredUsers.add(user);
        }
        return filteredUsers;
    }

    @NonNull
    private List<String> photoUrlsFromUsers(@NonNull List<UserModel> users) {
        final ArrayList<String> avatarUrls = new ArrayList<>(users.size());
        for (int i = 0; i < users.size(); i++) {
            avatarUrls.add(users.get(i).getThumbnailX2());
        }
        return avatarUrls;
    }

}