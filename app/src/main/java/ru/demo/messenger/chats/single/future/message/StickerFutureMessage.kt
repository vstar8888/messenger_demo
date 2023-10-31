package ru.demo.messenger.chats.single.future.message

import ru.demo.data.message.Sticker

class StickerFutureMessage(chatId: Long,
                           payload: String,
                           val sticker: Sticker
) : FutureMessage(chatId, payload)