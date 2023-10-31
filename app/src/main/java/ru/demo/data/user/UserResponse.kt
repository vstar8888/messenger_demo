package ru.demo.data.user

import com.google.gson.annotations.SerializedName
import ru.demo.data.network.BaseResponse
import ru.demo.messenger.data.user.UserModel

class UserResponse(
        @SerializedName("user")
        var user: UserModel
) : BaseResponse()
