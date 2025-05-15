package ru.alekseykonstantinov.telegrambot.privatechat;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import ru.alekseykonstantinov.interfaceImp.ChatHandler;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import static ru.alekseykonstantinov.utilites.Utilities.toPrettyJson;

@Slf4j
public class PrivateChat implements ChatHandler {
    private final MyBotTelegram bot;

    public PrivateChat(MyBotTelegram bot) {
        this.bot = bot;
    }

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getFrom() != null) {
                ChatMember chatMember = bot.getChatMember(update);
                log.info("Информация о member: {}", toPrettyJson(chatMember));
                log.info("Роль о приватном чате: {}", bot.getMemberRole(chatMember));
            }
            if (update.getMessage().getChat() != null) {
                Long chatId = update.getMessage().getChatId();
                ChatFullInfo chatFullInfo = bot.getChat(chatId);
                log.info("Информация о чате: \n{}", toPrettyJson(chatFullInfo));
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение в приватном чате: {}{}", " message:  ", message);
            Long chatId = update.getMessage().getChatId();
            bot.sendMessageGetChatId(chatId, message);

            //отправка изображения по названию
            if (message.equalsIgnoreCase("привет")
                    || message.equalsIgnoreCase("hello")) {
                bot.sendImageUploadingAFileJpg("ulybashka", chatId.toString());
            }
        }

        if (update.getMessage().hasSticker()) {
            String sticker_file_id = update.getMessage().getSticker().getFileId();
            System.out.println(sticker_file_id);
            bot.stickerSender(update, sticker_file_id);
        }
    }

}
