package ru.demo.messenger.data.message;

import androidx.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import ru.demo.data.message.Sticker;

public class MessageDeserializer implements JsonDeserializer<MessageModel> {
    @Override
    public MessageModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject messageJson = json.getAsJsonObject();

        final long id = messageJson.get("id").getAsLong();
        final long chatId = messageJson.get("chain_id").getAsLong();
        final String createdDate = messageJson.get("created_at").getAsString();
        final String direction = messageJson.get("direction").getAsString();
        final String status = messageJson.get("status").getAsString();

        final long authorId = messageJson.get("author_id").getAsLong();

        final String message = messageJson.get("body_html").getAsString();
        final AttachedFiles files = context.deserialize(messageJson.get("attached_files"), AttachedFiles.class);

        final String authorFullName = messageJson.get("author_full_name").getAsString();
        final String authorAvatarUrl = getFromJsonOrNull(messageJson, "author_avatar_url");
        final String authorAlias = messageJson.get("author_alias").getAsString();

        final String payload = getFromJsonOrNull(messageJson, "payload");
        final String messageType = messageJson.get("message_type").getAsString();

        final MessageModel originalMessage = context.deserialize(messageJson.get("original_message"),
                MessageModel.class
        );
        final Sticker sticker = context.deserialize(messageJson.get("sticker"),
                Sticker.class
        );

        return new MessageModel(id, chatId, createdDate, direction, status, authorId, message, files,
                authorFullName, authorAvatarUrl, authorAlias, payload, messageType, originalMessage,
                sticker
        );
    }

    @Nullable
    private String getFromJsonOrNull(JsonObject messageJson, String fieldName) {
        String result = null;
        if (!messageJson.get(fieldName).isJsonNull()) {
            result = messageJson.get(fieldName).getAsString();
        }
        return result;
    }

}
