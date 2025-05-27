package ru.alekseykonstantinov.telegrambot.privatechat;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.alekseykonstantinov.interfaceImp.ChatHandler;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import static ru.alekseykonstantinov.utilites.Utilities.*;

@Slf4j
public class PrivateChat implements ChatHandler {
    private final MyBotTelegram bot;

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
                case "/clearMenu" -> bot.clearBotCommandsScopeChat(chatId);
            }
            return;
        }

        // сведения при получении сообщений
        if (update.hasMessage()) {
            //bot.sendCustomKeyboard(update.getMessage().getChatId().toString());
            //bot.sendCustomForceReplyKeyboard(update.getMessage().getChatId().toString());
            //bot.sendInlineKeyboard(update.getMessage().getChatId().toString());
            //bot.hideKeyboard(update.getMessage().getChatId().toString());
            //bot.setCommandsMenu();

            //bot.clearBotCommands();
            //bot.logCurrentCommands();
//            try {
//                bot.setCommandsMenu();
//            } catch (TelegramApiException e) {
//                log.error("Что то не так с отправкой меню: {}", e.getMessage());
//            }
            technicalInfo(update);
            // отправка меню в приватный чат
            //bot.setCommandsScopePrivateChatMenu(bot.getChatId(update));
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение в приватном чате: {}{}", " message:  ", message);
            Long chatId = bot.getChatId(update);
            User user = update.getMessage().getFrom();


//            Message editMessage = bot.sendEditMessageChatId(msg, String.valueOf(message));
//            log.info("Результат изменения: {}", toPrettyJson(editMessage));

            //отправка изображения по приветствию
            if (getIsMessageArraysForms(message, messageGreeting)) {
                Message msg = messagePrints(chatId);
                String msgOut = String.format("Здравствуйте! %1s", bot.getInfoUserChat(user));
                bot.sendEditMessageChatId(msg, String.valueOf(msgOut));
                bot.sendImageUploadingAFileJpg("ulybashka", chatId);
                return;
            } else if (getIsMessageArraysForms(message, messageFormsOfFarewell)) {
                Message msg = messagePrints(chatId);
                String msgOut = String.format("До свидания! %1s", bot.getInfoUserChat(user));
                bot.sendEditMessageChatId(msg, String.valueOf(msgOut));
                return;
            } else if (getIsMessageArraysForms(message, messageCompliments)) {
                Message msg = messagePrints(chatId);
                String msgOut = String.format("%1s %2s", getRandomExpressionGratitude(), bot.getInfoUserChat(user));
                bot.sendEditMessageChatId(msg, String.valueOf(msgOut));
                return;
            }

            if (getIsMessageArraysForms(message, listTranslit)) {
                appealDialogflowTranslit(chatId, message);
                return;
            }

            // appealGPTChat(chatId, message);
            appealDialogflow(chatId, message);
            //appealYandexGPT(chatId, message);

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
     * Отправка запросов в Dialogflow
     */
    private void appealDialogflow(Long chatId, String message) {
        Message msg = messagePrints(chatId);
        String sessionId = "tg-" + chatId;
        try {
            String responseDialogFlow = bot.getDialogflow().detectIntent(sessionId, message, "ru-RU");
            Message messageOut = bot.sendEditMessageChatId(msg, String.valueOf(responseDialogFlow));
            if (messageOut == null) {
                bot.sendEditMessageChatId(msg, String.valueOf("Что - то не так"));
            }
        } catch (Exception e) {
            log.error("Что-то не так Dialogflow: {}", e.getMessage());
            bot.sendEditMessageChatId(msg, String.valueOf("Что-то не так"));
        }
    }

    /**
     * Отправка запросов в Dialogflow
     */
    private void appealDialogflowTranslit(Long chatId, String message) {
        Message msg = messagePrints(chatId);
        String sessionId = "tg-" + chatId;
        try {
            String responseDialogFlow = bot.getDialogflowTranslit().detectIntent(sessionId, message, "ru-RU");
            bot.sendEditMessageChatId(msg, String.valueOf(responseDialogFlow));
        } catch (Exception e) {
            log.error("Что-то не так DialogflowTranslit: {}", e.getMessage());
            bot.sendEditMessageChatId(msg, String.valueOf("Что-то не так"));
        }
    }

    /**
     * Отправка запросов в Dialogflow
     */
    private void appealYandexGPT(Long chatId, String message) {
        Message msg = messagePrints(chatId);

        try {
            String responseYandexGPT = bot.getYandexGPTClient().generateText(message);
            bot.sendEditMessageChatId(msg, String.valueOf(responseYandexGPT));
        } catch (Exception e) {
            log.error("Что-то не так YandexGPT: {}", e.getMessage());
            bot.sendEditMessageChatId(msg, String.valueOf("Что-то не так"));
        }
    }

    /**
     * Отправка запросов GPT chat
     *
     * @param chatId
     * @param message
     */
    private void appealGPTChat(Long chatId, String message) {
        String prompt = "Вести разговор от имени Alex AI Bot. Чат бот. на разные тематики";
        Message msg = messagePrints(chatId);
        try {
            String responseGpt = bot.getChatGPT().sendMessage(prompt, message);
            bot.sendEditMessageChatId(msg, String.valueOf(responseGpt));
        } catch (Exception e) {
            log.info("Что-то не так с chatGPT: {}", e.getMessage());
            bot.sendEditMessageChatId(msg, String.valueOf("Что-то не так"));
        }
    }

    /**
     * Предварительное сообщение
     *
     * @param chatId
     * @return
     */
    private Message messagePrints(Long chatId) {
        String mess = "Печатает...";
        Message message = bot.sendMessageGetChatId(chatId, String.valueOf(mess));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("что-то не так");
        }
        return message;
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
