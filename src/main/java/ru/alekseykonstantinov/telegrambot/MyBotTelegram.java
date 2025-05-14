package ru.alekseykonstantinov.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.alekseykonstantinov.telegrambot.group.WebFrontGroup;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_GROUP_FRONT_NAME;
import static ru.alekseykonstantinov.utilites.Utilities.getUserData;

@Slf4j
public class MyBotTelegram implements LongPollingSingleThreadUpdateConsumer {
    protected TelegramClient telegramClient;

    public MyBotTelegram(String TOKEN) {
        telegramClient = new OkHttpTelegramClient(TOKEN);
    }

//    @Override
//    public void consume(List<Update> updates) {
//        LongPollingSingleThreadUpdateConsumer.super.consume(updates);
//        //log.info(toPrettyJson(updates));
//    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение: {}", message);
            //Long chatId = update.getMessage().getChatId();
            //sendMessageGetChatId(chatId, message);
        }

        // обработка сообщений полученных от группы
        if (update.hasMessage()
                && update.getMessage().getChat().getType().equals("supergroup")
                && update.getMessage().getChat().getTitle().equals(TELEGRAM_BOT_GROUP_FRONT_NAME)) {
            new WebFrontGroup().consumeGroup(update);
        }

        //При добавлении нового участника
        if (update.hasMessage() && !update.getMessage().getNewChatMembers().isEmpty()) {

            Chat chat = update.getMessage().getChat();
            update.getMessage().getNewChatMembers().stream()
                    .forEach(user -> {
                        log.info("Новый пользователь метод update.getMessage().getNewChatMembers() {}", getUserData(user));
                        sendMessageNewUser(chat, user);
                    });
        }

        // Новый приватный чат с ботом
        if (update.hasMessage() && update.getMessage().hasEntities()
                && update.getMessage().getChat().getType().equals("private")
                && update.getMessage().getEntities().getFirst().getType().equals("bot_command")
                && update.getMessage().getEntities().getFirst().getText().equals("/start")) {
            Chat chat = update.getMessage().getChat();
            User user = update.getMessage().getFrom();
            log.info("Новый пользователь в приватном чате метод update.hasMyChatMember() {}", getUserData(user));
            sendMessageNewUser(chat, user);
        }

        // при удалении участника
        if (update.hasMessage() && update.getMessage().getLeftChatMember() != null) {
            User leftChatMemberUser = update.getMessage().getLeftChatMember();
            log.info("Пользователь удален из участников метод update.getMessage().getLeftChatMember() != null {} {}",
                    getUserData(leftChatMemberUser),
                    update.getMessage().getChat().getTitle());
        }
    }

    /**
     * Метод подготовки сообщение приветствие новому участнику
     *
     * @param chat чат
     * @param user новый участник или юзер
     */
    public void sendMessageNewUser(Chat chat, User user) {
        String msgGroup = chat.getType().equals("supergroup")
                ? "в группу " + chat.getTitle()
                : chat.getType().equals("private")
                ? "в " + chat.getType() + " чат"
                : "";

        String message = String.format(
                "@%1s Привет %2s. Добро пожаловать %3s",
                user.getUserName(),
                user.getFirstName(),
                msgGroup
        );
        sendMessageGetChatId(chat.getId(), message);
    }

    /**
     * Отправка сообщения в чат
     *
     * @param chatId чат ид
     * @param msg    сообщение
     */
    public void sendMessageGetChatId(Long chatId, String msg) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), msg);
        send(sendMessage);
    }

    /**
     * Отобразить ChatActions, такие как «набор текста» или «запись голосового сообщения»
     *
     * @param update событие
     */
    public void chatActions(Update update) {
        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        //sendChatAction.setChatId(update.getMessage().getChatId());
        ActionType actionType;

        if (text.equals("/type")) {
            // -> "typing"
            actionType = ActionType.TYPING;
            // -> "recording a voice message"
        } else if (text.equals("/record_audio")) {
            actionType = ActionType.RECORD_VOICE;
        } else {
            // -> more actions in the Enum ActionType
            // For information: https://core.telegram.org/bots/api#sendchataction
            actionType = ActionType.UPLOAD_DOCUMENT;
        }
        SendChatAction sendChatAction = new SendChatAction(chatId, actionType.name());
        send(sendChatAction);
    }

    /**
     * Отправка боту в зависимости от метода и чата
     *
     * @param method ограничены методами BotApiMethod
     * @param
     */
    public <T extends BotApiMethod> void send(T method) {
        try {
            telegramClient.execute(method);
            log.info("send: {}", "success");
        } catch (TelegramApiException e) {
            // TODO Auto-generated catch block
            log.error("Что то пошло не так send: {}", e.getMessage());
        }
    }

    public <T extends SendPhoto> void send(T method) {
        try {
            telegramClient.execute(method);
            log.info("send: {}", "success");
        } catch (TelegramApiException e) {
            // TODO Auto-generated catch block
            log.error("Что то пошло не так send: {}", e.getMessage());
        }
    }

    /**
     * Извлечет PhotoSize из фотографии,
     * отправленной боту (в нашем случае мы берем больший размер из предоставленных):
     *
     * @param update событие
     * @return PhotoSize object
     */
    public PhotoSize getPhoto(Update update) {
        // Check that the update contains a message and the message has a photo
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            // When receiving a photo, you usually get different sizes of it
            List<PhotoSize> photos = update.getMessage().getPhoto();

            // We fetch the bigger photo
            return photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
        }

        // Return null if not found
        return null;
    }

    /**
     * Метод обработает оба (у нас есть два варианта: file_path уже есть или нам нужно его получить)
     * варианта и вернет финальный file_path:
     *
     * @param photo PhotoSize
     * @return string path
     */
    public String getFilePath(PhotoSize photo) {
        Objects.requireNonNull(photo);

        if (photo.getFilePath() != null) { // If the file_path is already present, we are done!
            return photo.getFilePath();
        } else { // If not, let find it
            // We create a GetFile method and set the file_id from the photo
            GetFile getFileMethod = new GetFile(photo.getFileId());
            //getFileMethod.setFileId(photo.getFileId());
            try {
                // We execute the method using AbsSender::execute method.
                File file = telegramClient.execute(getFileMethod);
                // We now have the file_path
                return file.getFilePath();
            } catch (TelegramApiException e) {
                log.error("Ошибка получения пути к файлу: {}", e.getMessage());
            }
        }

        return null; // Just in case
    }

    /**
     * Теперь, когда у нас есть, file_path мы можем его скачать
     *
     * @param filePath String
     * @return java.io.File object
     */
    public java.io.File downloadPhotoByFilePath(String filePath) {
        try {
            // Download the file calling AbsSender::downloadFile method
            return telegramClient.downloadFile(filePath);
        } catch (TelegramApiException e) {
            log.error("Ошибка получения объекта для скачивания к файлу: {}", e.getMessage());
        }

        return null;
    }

    // отправка фото
    // url
    public void sendImageFromUrl(String url, String chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(url));
        // Set destination chat id
        //sendPhotoRequest.setChatId(chatId);
        // Set the photo url as a simple photo
        //sendPhotoRequest.setPhoto(new InputFile(url));
        send(sendPhotoRequest);
    }

    public void sendImageFromFileId(String fileId, String chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(fileId));
        // Set destination chat id
        //sendPhotoRequest.setChatId(chatId);
        // Set the photo url as a simple photo
        //sendPhotoRequest.setPhoto(new InputFile(fileId));
        send(sendPhotoRequest);
    }

    public void sendImageUploadingAFile(String filePath, String chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(filePath));
        // Set destination chat id
        //sendPhotoRequest.setChatId(chatId);
        // Set the photo file as a new photo (You can also use InputStream with a constructor overload)
        //sendPhotoRequest.setPhoto(new InputFile(new File(filePath)));
        send(sendPhotoRequest);
    }
}

