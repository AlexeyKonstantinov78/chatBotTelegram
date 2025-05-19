package ru.alekseykonstantinov.telegrambot.privatechat;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import ru.alekseykonstantinov.interfaceImp.ChatHandler;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import java.util.Arrays;
import java.util.List;

import static ru.alekseykonstantinov.utilites.Utilities.getIsMessageArrays;
import static ru.alekseykonstantinov.utilites.Utilities.toPrettyJson;

@Slf4j
public class PrivateChat implements ChatHandler {
    private final MyBotTelegram bot;
    private final List<String> MessageGreeting =
            Arrays.asList("Привет", "Hello", "Хай", "Салют", "Добрый", "Доброе", "Приветствую", "Здорово", "Приветик", "Дарова", "Хэлло", "✋🏻", "🖖🏻");

    public PrivateChat(MyBotTelegram bot) {
        this.bot = bot;
    }

    @Override
    public void handleUpdate(Update update) {

        // обработка команд в приватном чате
        if ((update.hasMessage() && update.getMessage().hasEntities()
                && update.getMessage().getEntities().getFirst().getType().equalsIgnoreCase("bot_command"))
                || (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null)
        ) {
            String command = update.hasMessage() ? update.getMessage().getText() : update.getCallbackQuery().getData();
            Long chatId = bot.getChatId(update);
            switch (command) {
                case "/markup" -> bot.sendKeyboardPrivatChat(chatId);
                case "/hide" -> bot.sendKeyboardHide(chatId);
                case "/inlineKeyboard", "/start" -> bot.sendInlineKeyboard(bot.getChatId(update));
            }
            return;
        }

        // сведения при получении сообщений
        if (update.hasMessage()) {
            //bot.sendCustomKeyboard(update.getMessage().getChatId().toString());
            //bot.sendCustomForceReplyKeyboard(update.getMessage().getChatId().toString());
            //bot.sendInlineKeyboard(update.getMessage().getChatId().toString());
            //bot.hideKeyboard(update.getMessage().getChatId().toString());

            //bot.clearBotCommands();
            //bot.logCurrentCommands();
//            try {
//                bot.setCommandsMenu();
//            } catch (TelegramApiException e) {
//                log.error("Что то не так с отправкой меню: {}", e.getMessage());
//            }
            technicalInfo(update);
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение в приватном чате: {}{}", " message:  ", message);
            Long chatId = bot.getChatId(update);
            bot.sendMessageGetChatId(chatId, message);

            //отправка изображения по названию при приветствии
            if (getIsMessageArrays(message, MessageGreeting)) {
                bot.sendImageUploadingAFileJpg("ulybashka", chatId);
            }
        }

        // при получении стикера возвращает этот стикер
        if (update.hasMessage() && update.getMessage().hasSticker()) {
            String sticker_file_id = update.getMessage().getSticker().getFileId();
            log.info("Стикер с fileId: {}", sticker_file_id);
            bot.stickerSender(update, sticker_file_id);
        }

        // при получении фото отправляет обратно фото
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            Long chat_id = update.getMessage().getChatId();
//            List<PhotoSize> photos = update.getMessage().getPhoto();
//            photos.stream().forEach(photoSize -> log.info(toPrettyJson(photoSize)));
            PhotoSize ps = bot.getPhoto(update);
            String photoFieldIdId = bot.getPhotoFieldId(ps);
            //отправка полученного изображения
            bot.sendImageFromFileId(photoFieldIdId, chat_id);
        }

        // отправка инлайн клавиатуру
        // bot.sendInlineKeyboard(bot.getChatId(update));
    }

    /**
     * Получение информации об участнике роль информация о чате
     *
     * @param update
     */
    private void technicalInfo(Update update) {
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
}
