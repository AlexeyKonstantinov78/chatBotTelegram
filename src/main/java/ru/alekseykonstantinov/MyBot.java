package ru.alekseykonstantinov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
public class MyBot implements LongPollingSingleThreadUpdateConsumer {
    TelegramClient telegramClient;

    public MyBot(String TOKEN) {
        telegramClient = new OkHttpTelegramClient(TOKEN);
    }

    @Override
    public void consume(List<Update> updates) {
        LongPollingSingleThreadUpdateConsumer.super.consume(updates);
        log.info(toPrettyJson(updates));
    }

    @Override
    public void consume(Update update) {
        log.info(update.toString());
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение: " + message);
            //Long chatId = update.getMessage().getChatId();
            //sendMessageGetChatId(chatId, message);
        }

        if (update.hasMessage() && !update.getMessage().getNewChatMembers().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Long chatId = update.getMessage().getChatId();
            update.getMessage().getNewChatMembers().stream().forEach(User -> {
                sb.append("id: " + User.getId() + " ");
                sb.append("firstName: " + User.getFirstName() + " ");
                sb.append("isBot: " + User.getIsBot() + " ");
                sb.append("userName: " + User.getUserName() + " ");

                String message = String.format(
                        "@%1s Привет %2s Добро пожаловать в группу ",
                        User.getUserName(),
                        User.getFirstName()
                );
                sendMessageGetChatId(chatId, message);
            });
            log.info("Новый пользователь метод update.getMessage().getNewChatMembers() " + sb.toString());

        }

        // при удалении из участника
        if (update.hasMessage() && update.getMessage().getLeftChatMember() != null) {
            StringBuilder sb = new StringBuilder();
            User leftChatMember = update.getMessage().getLeftChatMember();

            sb.append("id: " + leftChatMember.getId() + " ");
            sb.append("firstName: " + leftChatMember.getFirstName() + " ");
            sb.append("isBot: " + leftChatMember.getIsBot() + " ");
            sb.append("userName: " + leftChatMember.getUserName() + " ");

            log.info("Пользователь удален из участников метод update.getMessage().getLeftChatMember() != null " + sb.toString());
        }
    }

    public void sendMessageGetChatId(Long chatId, String msg) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), msg);
        try {
            // Execute it
            telegramClient.execute(sendMessage);
            log.info("chatId: " + chatId + "; Отправлено сообщение: " + msg);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    public void printUpdate(List<Update> updates) {
        System.out.println(updates.get(0));
    }

    public String toPrettyJson(List<Update> updates) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(updates);
    }
}

