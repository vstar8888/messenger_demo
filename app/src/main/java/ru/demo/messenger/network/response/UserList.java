package ru.demo.messenger.network.response;

import java.util.List;

import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.network.response.base.PagedResponse;

public class UserList extends PagedResponse {
    public List<UserModel> users;
}
