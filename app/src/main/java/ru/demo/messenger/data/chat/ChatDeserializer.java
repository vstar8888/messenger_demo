package ru.demo.messenger.data.chat;

//public class ChatDeserializer implements JsonDeserializer<ChatModel> {
//    @Override
//    public ChatModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//        final JsonObject chatJson = json.getAsJsonObject();
//
//        final UserModel[] users = context.deserialize(chatJson.get("users"), UserModel[].class);
//        final MessageModel message = context.deserialize(chatJson, MessageModel.class);
//
//        final long chatId = chatJson.get("chain_id").getAsLong();
//        final int chatType = chatJson.get("chain_type").getAsInt();
//        final boolean allUsersRead = chatJson.get("all_users_as_read").getAsBoolean();
//        return new ChatModel(chatId, chatType, Arrays.asList(users), message);
//    }
//}
