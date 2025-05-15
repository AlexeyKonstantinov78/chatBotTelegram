package ru.alekseykonstantinov.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.alekseykonstantinov.interfaceImp.ChatHandler;
import ru.alekseykonstantinov.telegrambot.group.WebFrontGroup;
import ru.alekseykonstantinov.telegrambot.privatechat.PrivateChat;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_GROUP_FRONT_NAME;
import static ru.alekseykonstantinov.utilites.Utilities.getUserData;

@Slf4j
public class MyBotTelegram implements LongPollingSingleThreadUpdateConsumer {
    protected TelegramClient telegramClient;
    private final String TOKEN;
    private final ChatHandler webFrontGroup;
    private final ChatHandler privateChat;

    public MyBotTelegram(String TOKEN) {
        telegramClient = new OkHttpTelegramClient(TOKEN);
        this.TOKEN = TOKEN;
        this.webFrontGroup = new WebFrontGroup(this);
        this.privateChat = new PrivateChat(this);
    }

//    @Override
//    public void consume(List<Update> updates) {
//        LongPollingSingleThreadUpdateConsumer.super.consume(updates);
//        log.info(toPrettyJson(updates));
//    }

    @Override
    public void consume(Update update) {
        //ChatActions, такие как «набор текста» или «запись голосового сообщения»
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatActions(update);
        }

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
            //new WebFrontGroup().consumeGroup(update);
            webFrontGroup.handleUpdate(update);
        }

        // обработка сообщений полученных от приватного чата
        if (update.hasMessage() && update.getMessage().getChat().getType().equalsIgnoreCase("private")) {
            //new PrivateChat().consumePrivate(update);
            privateChat.handleUpdate(update);
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

    /**
     * Отправка фото по url
     *
     * @param url    ссылка на фото
     * @param chatId идентификатор чата
     */
    public void sendImageFromUrl(String url, String chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(url));
        send(sendPhotoRequest);
    }

    /**
     * Отправка фото по url
     *
     * @param fileId идентификатор фото на серверах telegram
     * @param chatId идентификатор чата
     */
    public void sendImageFromFileId(String fileId, String chatId) {
        SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(fileId));
        send(sendPhotoRequest);
    }

    /**
     * Метод для отправки изображение в чат
     *
     * @param name   имя файла
     * @param chatId идентификатор чата
     */
    public void sendImageUploadingAFileJpg(String name, String chatId) {
        sendImageUploadingAFile(getPathResourcesImageNameJpg(name), chatId);
    }

    /**
     * Отправка фото из ресурса
     *
     * @param filePath путь к фото /image...
     * @param chatId   идентификатор чата
     */
    public void sendImageUploadingAFile(String filePath, String chatId) {
        java.io.File file = new java.io.File(filePath);
        // Проверка существования файла
        if (!file.exists()) {
            log.error("Файл не найден: {}", filePath);
        }
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(file));
        send(sendPhotoRequest);
    }

    /**
     * Путь к файлу в ресурсе по названию
     *
     * @param name имя файла без расширения по умолчанию jpg
     * @return возвращает путь к файлу
     */
    public String getPathResourcesImageNameJpg(String name) {
        try {
            return MyBotTelegram.class.getClassLoader().getResource("image/" + name + ".jpg").getPath();
        } catch (NullPointerException e) {
            log.error("Стаким именем файла скорее всего нет: {}", name);
            return "";
        }
    }

    /**
     * Получение данных о чате
     *
     * @param chatId ид чата
     * @return class ChatFullInfo
     */
    public ChatFullInfo getChat(Long chatId) {
        GetChat getChat = new GetChat(chatId.toString());
        ChatFullInfo infoChat;
        try {
            infoChat = telegramClient.execute(getChat);
        } catch (TelegramApiException e) {
            log.error("Не получили информацию о чате: {}", e.getMessage());
            return null;
        }
        return infoChat;
    }

    /**
     * Получение списка Администраторов и владельца группы
     *
     * @param update данные при событии
     */
    public List<ChatMember> getChatAdministrators(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        List<ChatMember> administrators;

        try {
            GetChatAdministrators getChatAdministrators = new GetChatAdministrators(chatId.toString());
            administrators = telegramClient.execute(getChatAdministrators);

        } catch (TelegramApiException e) {
            log.error("Ошибка получения списка админов данных: {}", e.getMessage());
            return null;
        }
        return administrators;
    }

    /**
     * Получает данные member из группы которые разрешены
     *
     * @param update событие
     * @return class ChatMember
     */
    public ChatMember getChatMember(Update update) {
        ChatMember chatMemberUser;
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        GetChatMember getChatMember = new GetChatMember(chatId.toString(), userId);
        try {
            chatMemberUser = telegramClient.execute(getChatMember);
        } catch (TelegramApiException e) {
            log.error("Ошибка получения данных члена: {}", e.getMessage());
            return null;
        }

        return chatMemberUser;
    }

    /**
     * Получаем количество участников группы
     *
     * @param update событие данные
     * @return количество members
     */
    public Integer getChatMemberCount(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        Integer chatMemberCount;

        try {
            GetChatMemberCount getChatMemberCount = new GetChatMemberCount(chatId.toString());
            chatMemberCount = telegramClient.execute(getChatMemberCount);

        } catch (TelegramApiException e) {
            log.error("Ошибка получения количества членов группы: {}", e.getMessage());
            return null;
        }
        return chatMemberCount;
    }

    /**
     * Роли группы участников
     *
     * @param member данные участника
     * @return тип участника
     */
    public String getMemberRole(ChatMember member) {
        if (member instanceof ChatMemberOwner) {
            return "Владелец";
        } else if (member instanceof ChatMemberAdministrator) {
            return "Администратор";
        } else {
            return "Участник";
        }
    }
}

