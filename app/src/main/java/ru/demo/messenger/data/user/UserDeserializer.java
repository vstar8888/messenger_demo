package ru.demo.messenger.data.user;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UserDeserializer implements JsonDeserializer<UserModel> {
    final Gson gson = new Gson();

    @Override
    public UserModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject userJson = json.getAsJsonObject();
        final UserModel user = gson.fromJson(userJson, UserModel.class);
        if (!userJson.has("full_name_short")) {
            // if short_user passed
            final JsonElement url_photo = userJson.get("url_photo");
            if (url_photo.isJsonPrimitive()) {
                user.setThumbnail(url_photo.getAsString());
            }
            final JsonElement url_photo_x2 = userJson.get("url_photo_x2");
            if (url_photo_x2.isJsonPrimitive()) {
                user.setThumbnailX2(url_photo_x2.getAsString());
            }
        }
        return user;
    }
}
