package ru.alekseykonstantinov.telegrambot.privatechat;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
        //bot.sendInlineKeyboard(bot.getChatId(update));

        // обработка команд в приватном чате
        if (update.hasMessage()
                && update.getMessage().hasEntities()
                && update.getMessage().getEntities().getFirst().getType().equalsIgnoreCase("bot_command")) {
            String command = update.getMessage().getText();
            Long chatId = bot.getChatId(update);
            switch (command) {
                case "/markup" -> sendKeyboard(chatId);
                case "/hide" -> bot.sendKeyboardHide(chatId);
            }
            return;
        }

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
            Long chatId = bot.getChatId(update);
            bot.sendMessageGetChatId(chatId, message);

            //отправка изображения по названию
            if (message.equalsIgnoreCase("привет")
                    || message.equalsIgnoreCase("hello")) {
                bot.sendImageUploadingAFileJpg("ulybashka", chatId);
            }
        }

        if (update.getMessage().hasSticker()) {
            String sticker_file_id = update.getMessage().getSticker().getFileId();
            System.out.println(sticker_file_id);
            bot.stickerSender(update, sticker_file_id);
        }

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            Long chat_id = update.getMessage().getChatId();
//            List<PhotoSize> photos = update.getMessage().getPhoto();
//            photos.stream().forEach(photoSize -> log.info(toPrettyJson(photoSize)));
            PhotoSize ps = bot.getPhoto(update);
            String photoFieldIdId = bot.getPhotoFieldId(ps);
            //отправка полученного изображения
            bot.sendImageFromFileId(photoFieldIdId, chat_id.toString());

        }
    }

    /**
     * Добавляем клавиатуру в приватный чат
     *
     * @param chatId ид чата
     */
    private void sendKeyboard(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Твоя клавиатура")
                .build();

        message.setReplyMarkup(ReplyKeyboardMarkup
                .builder()
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .keyboardRow(new KeyboardRow("Row 1 Button 1", "Row 1 Button 2", "Row 1 Button 3"))
                .keyboardRow(new KeyboardRow("Row 1 Button 1", "Row 1 Button 2", "Row 1 Button 3"))

                .build()
        );
        bot.send(message);
    }


}
