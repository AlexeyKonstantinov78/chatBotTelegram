package ru.alekseykonstantinov.telegrambot.privatechat;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.UserProfilePhotos;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alekseykonstantinov.interfaceImp.ChatHandler;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import java.util.Comparator;

import static ru.alekseykonstantinov.utilites.Utilities.toPrettyJson;

@Slf4j
public class BusinessPrivetChat implements ChatHandler {
    private final MyBotTelegram bot;
    private String botName;
    private String botUserName;
    private Long botId;
    private String botPhotoFieldId;


    public BusinessPrivetChat(MyBotTelegram bot) {
        this.bot = bot;
        getBotInfo();
    }

    @Override
    public void handleUpdate(Update update) {
        log.info("Приватный чат бизнес Bot");
        String message = bot.getChatText(update);
        Long chatId = bot.getChatId(update);
        String businessConnectionId = bot.getBusinessConnectionId(update);
        User user = update.getBusinessMessage().getFrom();
        String capture = String.format("@%1s %2s", botUserName, botName);

        if (!message.isEmpty() && (message.equalsIgnoreCase("Привет")
                || message.equalsIgnoreCase("Hello")
                || message.equalsIgnoreCase("Хай")
                || message.equalsIgnoreCase("Салют")
                || message.equalsIgnoreCase("Добрый день")
                || message.equalsIgnoreCase("Доброе утро"))
        ) {
            String outMsg = String.format("Здравствуйте %1s %2s \uD83D\uDD96\uD83C\uDFFB\uD83D\uDE4F %3s",
                    user.getFirstName(),
                    user.getLastName(),
                    capture
            );
            bot.sendMessageGetChatId(chatId, businessConnectionId, outMsg);
            // bot.sendImageFromFileId(botPhotoFieldId, chatId, String.format("@%1s %2s", botUserName, botName), businessConnectionId);

        }
    }

    private void getBotInfo() {
        GetMe getMe = GetMe.builder().build();

        PhotoSize botPhotoSize = new PhotoSize();
        try {
            User userBot = bot.telegramClient.execute(getMe);
            botName = userBot.getFirstName();
            botId = userBot.getId();
            botUserName = userBot.getUserName();

            GetUserProfilePhotos getUserProfilePhotos = GetUserProfilePhotos.builder()
                    .userId(userBot.getId())
                    .build();

            UserProfilePhotos sendGetUserProfilePhotos = bot.telegramClient.execute(getUserProfilePhotos);

            botPhotoSize = sendGetUserProfilePhotos.getPhotos().getFirst()
                    .stream()
                    .min(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);

            log.info(toPrettyJson(userBot));
            log.info(toPrettyJson(sendGetUserProfilePhotos));
            log.info(toPrettyJson(botPhotoSize));
        } catch (TelegramApiException e) {
            log.error("Что то не так с получением информации о чат боте: {}", e.getMessage());
        }


        botPhotoFieldId = bot.getPhotoFieldId(botPhotoSize);
    }
}
