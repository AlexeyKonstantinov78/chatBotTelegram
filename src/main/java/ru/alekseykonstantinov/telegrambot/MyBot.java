package ru.alekseykonstantinov.telegrambot;

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

        // при добавлении нового участника
        if (update.hasMessage() && !update.getMessage().getNewChatMembers().isEmpty()) {

            Long chatId = update.getMessage().getChatId();
            update.getMessage().getNewChatMembers().stream().forEach(user -> {
                log.info("Новый пользователь метод update.getMessage().getNewChatMembers() " + getUserData(user));
                sendMessageNewUser(chatId, user);
            });
        }

        // при удалении из участника
        if (update.hasMessage() && update.getMessage().getLeftChatMember() != null) {
            StringBuilder sb = new StringBuilder();
            User leftChatMemberUser = update.getMessage().getLeftChatMember();
            log.info("Пользователь удален из участников метод update.getMessage().getLeftChatMember() != null " + getUserData(leftChatMemberUser));
        }
    }

    /**
     * @param user юзер
     *             Метод для получения данных юзера
     */
    public String getUserData(User user) {
        StringBuilder sb = new StringBuilder();

        sb.append("id: " + user.getId() + " ");
        sb.append("firstName: " + user.getFirstName() + " ");
        sb.append("isBot: " + user.getIsBot() + " ");
        sb.append("userName: " + user.getUserName() + " ");

        return sb.toString();
    }

    /**
     * Метод подготовки сообщение приветствие новому участнику
     *
     * @param chatId чат ид
     * @param user   новый участник или юзер
     */
    public void sendMessageNewUser(Long chatId, User user) {
        String message = String.format(
                "@%1s Привет %2s Добро пожаловать в группу ",
                user.getUserName(),
                user.getFirstName()
        );
        sendMessageGetChatId(chatId, message);
    }

    /**
     * Отправка сообщения в чат
     *
     * @param chatId чат ид
     * @param msg    сообщение
     */
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

    /**
     * Разбор ответа на json формат
     *
     * @param updates список события
     * @return возврат в json формате
     */
    public String toPrettyJson(List<Update> updates) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(updates);
    }
}

