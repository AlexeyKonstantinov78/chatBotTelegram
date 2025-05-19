package ru.alekseykonstantinov.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.GetMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.alekseykonstantinov.interfaceImp.ChatHandler;
import ru.alekseykonstantinov.telegrambot.group.WebFrontGroup;
import ru.alekseykonstantinov.telegrambot.privatechat.BusinessPrivetChat;
import ru.alekseykonstantinov.telegrambot.privatechat.PrivateChat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static ru.alekseykonstantinov.utilites.Utilities.getUserData;
import static ru.alekseykonstantinov.utilites.Utilities.toPrettyJson;

@Slf4j
public class MyBotTelegram implements LongPollingSingleThreadUpdateConsumer {
    public TelegramClient telegramClient;
    private final String TOKEN;
    private final ChatHandler webFrontGroup;
    private final ChatHandler privateChat;
    private final ChatHandler businessPrivetChat;

    public MyBotTelegram(String TOKEN) {
        telegramClient = new OkHttpTelegramClient(TOKEN);
        this.TOKEN = TOKEN;
        this.webFrontGroup = new WebFrontGroup(this);
        this.privateChat = new PrivateChat(this);
        this.businessPrivetChat = new BusinessPrivetChat(this);
    }

    @Override
    public void consume(Update update) {
        clearBotCommands();
        logCurrentCommands();
        technicalInfo(update);

        if (update.hasMessage() && update.getMessage().hasText()) {
            //ChatActions, такие как «набор текста» или «запись голосового сообщения»
            chatActions(update);
            String message = update.getMessage().getText();
            log.info("Получено сообщение: {}", message);
            //Long chatId = update.getMessage().getChatId();
            //sendMessageGetChatId(chatId, message);
        }

        // обработка сообщений полученных от группы
        if (update.hasMessage()
                && (update.getMessage().getChat().isSuperGroupChat() || update.getMessage().getChat().isGroupChat())) {
            //new WebFrontGroup().consumeGroup(update);
            webFrontGroup.handleUpdate(update);
        }

        // обработка сообщений полученных от приватного чата
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()) {
            //new PrivateChat().consumePrivate(update);
            privateChat.handleUpdate(update);
        }

        // обработка сообщений полученных от BusinessMessage Bot приватного чата
        if (update.hasBusinessMessage() && update.getBusinessMessage().getChat().isUserChat()) {
            businessPrivetChat.handleUpdate(update);
        }

        // обработка сообщений полученных от BusinessMessage Bot приватного чата
        if (update.hasBusinessMessage()
                && (update.getBusinessMessage().getChat().isSuperGroupChat() || update.getBusinessMessage().getChat().isGroupChat())) {
            log.info("чат supergroup и group группа бизнес Bot");
        }

        //При добавлении нового участника приветствие в группе
        if (update.hasMessage() && !update.getMessage().getNewChatMembers().isEmpty()) {
            Chat chat = update.getMessage().getChat();
            update.getMessage().getNewChatMembers().stream()
                    .forEach(user -> {
                        sendMessageNewUser(chat, user);
                    });
        }

        // Новый приватный чат с ботом приветствие
        if (update.hasMessage() && update.getMessage().hasEntities()
                && update.getMessage().getChat().isUserChat()
                && update.getMessage().getEntities().getFirst().getType().equals("bot_command")
                && update.getMessage().getEntities().getFirst().getText().equals("/start")) {
            Chat chat = update.getMessage().getChat();
            User user = update.getMessage().getFrom();

            sendMessageNewUser(chat, user);
        }
    }

    /**
     *
     */
    public void technicalInfo(Update update) {
        log.info(toPrettyJson(update));

        log.info("ChannelPost: {}", update.hasChannelPost());

        try {
            log.info("Message isChannelChat: {}", update.getMessage().getChat().isChannelChat());
            log.info("Message isUserChat: {}", update.getMessage().getChat().isUserChat());
            log.info("Message isGroupChat: {}", update.getMessage().getChat().isGroupChat());
            log.info("Message isSuperGroupChat: {}", update.getMessage().getChat().isSuperGroupChat());
        } catch (Exception e) {
            log.info("Message: {}", e.getMessage());
        }

        try {
            log.info("BusinessMessage isChannelChat: {}", update.getBusinessMessage().getChat().isChannelChat());
            log.info("BusinessMessage isUserChat: {}", update.getBusinessMessage().getChat().isUserChat());
            log.info("BusinessMessage isGroupChat: {}", update.getBusinessMessage().getChat().isGroupChat());
            log.info("BusinessMessage isSuperGroupChat: {}", update.getBusinessMessage().getChat().isSuperGroupChat());
        } catch (Exception e) {
            log.info("BusinessMessage: {}", e.getMessage());
        }

        log.info("BusinessConnection: {}", update.hasBusinessConnection());

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
        Boolean isGroup = chat.isSuperGroupChat();
        String msgGroup = isGroup
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

        if (isGroup) {
            log.info("Новый пользователь в группе {} {}", chat.getTitle(), getUserData(user));
        } else {
            log.info("Новый пользователь в приватном чате {}", getUserData(user));
        }

        sendMessageGetChatId(chat.getId(), message);
    }

    /**
     * Отправка сообщения в чат
     *
     * @param chatId чат ид
     * @param msg    сообщение
     */
    public void sendMessageGetChatId(Long chatId, String msg) {
        //SendMessage sendMessage = new SendMessage(chatId.toString(), msg);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(msg)
                .build();
        send(sendMessage);
    }

    /**
     * Отправка сообщения в BusinessMessage Bot чат
     *
     * @param chatId чат ид
     * @param msg    сообщение
     */
    public void sendMessageGetChatId(Long chatId, String businessConnectionId, String msg) {
        //SendMessage sendMessage = new SendMessage(chatId.toString(), msg);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .businessConnectionId(businessConnectionId)
                .text(msg)
                .build();
        send(sendMessage);
    }

    /**
     * Отобразить ChatActions, такие как «набор текста» или «запись голосового сообщения»
     *
     * @param update событие
     */
    public void chatActions(Update update) {
        String text = getChatText(update);
        Long chatId = getChatId(update);
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
        //SendChatAction sendChatAction = new SendChatAction(chatId, actionType.name());
        SendChatAction sendChatAction = SendChatAction.builder()
                .chatId(chatId)
                .action(actionType.name())
                .build();
        send(sendChatAction);
    }

    /**
     * Получение чата ид
     *
     * @return chatId
     */
    public Long getChatId(Update update) {

        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }

        if (update.hasBusinessMessage()) {
            return update.getBusinessMessage().getChatId();
        }
        return null;
    }

    /**
     * Получение чата ид
     *
     * @return chatId
     */
    public String getChatText(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getText();
        }

        if (update.hasBusinessMessage()) {
            return update.getBusinessMessage().getText();
        }
        return null;
    }

    public String getBusinessConnectionId(Update update) {
        return update.getBusinessMessage().getBusinessConnectionId();
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

    public String getPhotoFieldId(PhotoSize photoSize) {
        return photoSize.getFileId();
    }

    /**
     * Метод обработает оба (у нас есть два варианта: file_path уже есть или нам нужно его получить)
     * варианта и вернет финальный file_path:
     *
     * @param photo PhotoSize
     * @return string path
     */
    public String getFilePhotoPath(PhotoSize photo) {
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
    public void sendImageFromUrl(String url, Long chatId) {
        // Create send method
        SendPhoto sendPhotoRequest = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(url))
                .build();
        send(sendPhotoRequest);
    }

    /**
     * Отправка фото по url
     *
     * @param fileId идентификатор фото на серверах telegram
     * @param chatId идентификатор чата
     */
    public void sendImageFromFileId(String fileId, String chatId) {
        SendPhoto sendPhotoRequest = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(fileId))
                .build();
        //SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(fileId));
        send(sendPhotoRequest);
    }

    /**
     * Отправка фото по fileId business чат
     *
     * @param fileId               идентификатор фото на серверах telegram
     * @param chatId               идентификатор чата
     * @param caption              описание или подпись
     * @param businessConnectionId id business чата     *
     */
    public void sendImageFromFileId(String fileId, Long chatId, String caption, String businessConnectionId) {
        SendPhoto sendPhotoRequest = SendPhoto.builder()
                .chatId(chatId)
                .businessConnectionId(businessConnectionId)
                .photo(new InputFile(fileId))
                .caption(caption)
                .build();
        //SendPhoto sendPhotoRequest = new SendPhoto(chatId, new InputFile(fileId));
        send(sendPhotoRequest);
    }

    /**
     * Метод для отправки изображение в чат
     *
     * @param name   имя файла
     * @param chatId идентификатор чата
     */
    public void sendImageUploadingAFileJpg(String name, Long chatId) {
        sendImageUploadingAFile(getPathResourcesImageNameJpg(name), chatId);
    }

    /**
     * Отправка фото из ресурса
     *
     * @param filePath путь к фото /image...
     * @param chatId   идентификатор чата
     */
    public void sendImageUploadingAFile(String filePath, Long chatId) {
        java.io.File file = new java.io.File(filePath);
        // Проверка существования файла
        if (!file.exists()) {
            log.error("Файл не найден: {}", filePath);
        }
        // Create send method
        SendPhoto sendPhotoRequest = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(file))
                .build();
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
        GetChat getChat = GetChat.builder()
                .chatId(chatId)
                .build();
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
        Long chatId = getChatId(update);
        List<ChatMember> administrators;

        try {
            GetChatAdministrators getChatAdministrators = GetChatAdministrators.builder()
                    .chatId(chatId)
                    .build();
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
        Long chatId = getChatId(update);
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
        Long chatId = getChatId(update);
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

    /**
     * Отправить sticker
     *
     * @param update
     * @param Sticker_file_id ид на серверах telegram
     */
    public void stickerSender(Update update, String Sticker_file_id) {
        //the ChatId that  we received form Update class
        Long chatId = getChatId(update);
        // Create an InputFile containing Sticker's file_id or URL
        InputFile stickerFile = new InputFile(Sticker_file_id);
        // Create a SendSticker object using the ChatId and StickerFile
        //SendSticker TheSticker = new SendSticker(ChatId, StickerFile);
        SendSticker theSticker = SendSticker.builder()
                .chatId(chatId)
                .sticker(stickerFile)
                .build();

        // Will reply the sticker to the message sent
        //TheSticker.setReplyToMessageId(update.getMessage().getMessageId());

        try {  // Execute the method
            telegramClient.execute(theSticker);

        } catch (TelegramApiException e) {
            log.error("Ошибка отправки стикера: {}", e.getMessage());
        }
    }

    //пользовательские клавиатуры

    /**
     * Пользовательские клавиатуры можно добавлять к сообщениям с помощью setReplyMarkup.
     * В этом примере мы создадим простую ReplyKeyboardMarkup с двумя строками и тремя кнопками в каждой строке,
     * но вы также можете использовать другие типы, такие как ReplyKeyboardHide, ForceReply или InlineKeyboardMarkup :
     *
     * @param chatId
     */
    public void sendCustomKeyboard(String chatId) {
        SendMessage message = new SendMessage(chatId, "Вот клавиатура");
        //        message.setChatId(chatId);
        //        message.setText("Custom message text");

        // Create the keyboard (list of keyboard rows)
        List<KeyboardRow> keyboard = new ArrayList<>();
        // Create a keyboard row
        KeyboardRow row = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add("Row 1 Button 1");
        row.add("Row 1 Button 2");
        row.add("Row 1 Button 3");
        // Add the first row to the keyboard
        keyboard.add(row);
        // Create another keyboard row
        row = new KeyboardRow();
        // Set each button for the second line
        row.add("Row 2 Button 1");
        row.add("Row 2 Button 2");
        row.add("Row 2 Button 3");
        // Add the second row to the keyboard
        keyboard.add(row);

        // Create ReplyKeyboardMarkup object
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboard);
        keyboardMarkup.setResizeKeyboard(true); // подгоняем размер
        keyboardMarkup.setOneTimeKeyboard(false); // клавиатура останется после использования
        // Set the keyboard to the markup
        //keyboardMarkup.setKeyboard(keyboard);
        // Add it to the message
        message.setReplyMarkup(keyboardMarkup);
        send(message);
    }

    /**
     * Скрывает пользовательскую клавиатуру
     *
     * @param chatId ид чата
     */
    public void sendKeyboardHide(Long chatId) {
        // Hide the keyboard
        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text("Keyboard hidden")
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
        send(message);
    }

    /**
     * InlineKeyboardMarkup использует список для захвата кнопок вместо KeyboardRow.
     *
     * @param chatId
     */
    public void sendInlineKeyboard(Long chatId) {
        // Создаем сообщение с текстом
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Клавиатура")
                .build();

        // Создаем кнопки через билдер
        InlineKeyboardButton youtubeBtn = InlineKeyboardButton.builder()
                .text("Показать клавиатуру")
//                .copyText("/markup")
                .url("https://youtube.com")
                .build();

        InlineKeyboardButton githubBtn = InlineKeyboardButton.builder()
                .text("Убрать клавиатуру")
                //.url("https://github.com")
                .switchInlineQuery("/hide")
                .build();

        // Создаем ряды кнопок (важно использовать InlineKeyboardRow)
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        // Создаем первый ряд и добавляем кнопки
        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(youtubeBtn);
        row1.add(githubBtn);
        keyboard.add(row1);

        // Создаем разметку клавиатуры
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)  // Теперь передаем List<InlineKeyboardRow>
                .build();

        message.setReplyMarkup(keyboardMarkup);
        send(message);
    }

    /**
     * ForceReplyKeyboard
     *
     * @param chatId
     */
    public void sendCustomForceReplyKeyboard(Long chatId) {

        //SendMessage message = new SendMessage(chatId, "Пожалуйста, введите ваш ответ:");
        String text = "Пожалуйста, введите ваш ответ:";
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        // Создание ForceReply клавиатуры
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setInputFieldPlaceholder("Введите текст здесь...");
        forceReplyKeyboard.setSelective(false); // Показывать только определенным пользователям
        message.setReplyMarkup(forceReplyKeyboard);

        send(message);
    }

    /**
     * Меню с командами (команды в списке /menu)
     * new BotCommandScopeDefault() область видимости меню
     * Telegram Bot API предоставляет и другие варианты областей видимости:
     * BotCommandScopeAllPrivateChats - только приватные чаты
     * BotCommandScopeAllGroupChats - только групповые чаты
     * BotCommandScopeAllChatAdministrators - только администраторы чатов
     * BotCommandScopeChat - для конкретного чата
     * BotCommandScopeChatAdministrators - только администраторы конкретного чата
     * BotCommandScopeChatMember - для конкретного пользователя в конкретном чате
     * languageCode - код языка (например, "en", "ru", "es" и т.д.)
     *
     * @throws TelegramApiException api
     */
    public void setCommandsMenu() throws TelegramApiException {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "начать работу"));
        commands.add(new BotCommand("/help", "помощь"));
        commands.add(new BotCommand("/settings", "настройки"));
        commands.add(new BotCommand("/info", "информация о боте"));
        SetMyCommands setMyCommands = new SetMyCommands(commands, new BotCommandScopeDefault(), "ru");

        //bot.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        send(setMyCommands);

        try {
            // Send the message
            telegramClient.execute(setMyCommands);
            log.info("Menu отправлено");
        } catch (TelegramApiException e) {
            log.error("Что-то пошло не так с отправкой кнопок menu: {}", e.getMessage());
        }
    }

    /**
     * Удаление всех команд (очистка меню)
     */
    public void clearBotCommands() {
        DeleteMyCommands deleteMyCommandsBotCommandScopeDefault = new DeleteMyCommands();
        DeleteMyCommands deleteMyCommandsBotCommandScopeAllPrivateChats = new DeleteMyCommands();
        DeleteMyCommands deleteMyCommandsBotCommandScopeAllGroupChats = new DeleteMyCommands();

        deleteMyCommandsBotCommandScopeDefault.setScope(new BotCommandScopeDefault());
        deleteMyCommandsBotCommandScopeAllPrivateChats.setScope(new BotCommandScopeAllPrivateChats());
        deleteMyCommandsBotCommandScopeAllGroupChats.setScope(new BotCommandScopeAllGroupChats());

        // Пустой список команд
//        List<BotCommand> commands = new ArrayList<>();
//        commands.add(new BotCommand("", ""));
//        SetMyCommands setMyCommands = new SetMyCommands(commands, new BotCommandScopeDefault(), "ru");

//        List<BotCommand> commands = new ArrayList<>();
//        commands.add(new BotCommand("/start", "начать работу"));
//
//        // Используем новый конструктор SetMyCommands
//        SetMyCommands setMyCommands = SetMyCommands.builder()
//                .commands(commands)
//                .scope(new BotCommandScopeDefault())
//                .build();
        try {
            // Send the message
            telegramClient.execute(deleteMyCommandsBotCommandScopeDefault);
            telegramClient.execute(deleteMyCommandsBotCommandScopeAllPrivateChats);
            telegramClient.execute(deleteMyCommandsBotCommandScopeAllGroupChats);
            log.info("Menu clear");
        } catch (TelegramApiException e) {
            log.error("Что-то пошло не так с очисткой кнопок menu: {}", e.getMessage());
        }
    }

    /**
     * Проверка текущих команд
     */
    public void logCurrentCommands() {
        try {
            GetMyCommands getCommands = new GetMyCommands();
            List<BotCommand> commands = telegramClient.execute(getCommands);
            log.info("Список команд: {}", toPrettyJson(commands));

        } catch (TelegramApiException e) {
            log.error("Что-то пошло не так с получением списка команд: {}", e.getMessage());
        }
    }

    /**
     * ReplyKeyboardHide в более старых версиях API был переименован в ReplyKeyboardRemove
     * скрытие клавиатуры
     *
     * @param chatId id
     */
    public void hideKeyboard(String chatId) {
        SendMessage message = new SendMessage(chatId, "Скрытие клавиатуры: ");

        // Создаем объект для скрытия клавиатуры
        ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder()
                .removeKeyboard(true)
                .selective(false)
                .build();

        message.setReplyMarkup(keyboardRemove);
        try {
            // Send the message
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Что-то пошло не так с отправкой кнопок Keyboard: {}", e.getMessage());
        }
    }


}

