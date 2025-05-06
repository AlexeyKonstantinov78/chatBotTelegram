package ru.alekseykonstantinov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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
            log.info(message);
            Long chatId = update.getMessage().getChatId();
            sendMessageGetChatId(chatId, message);
        }
    }

    public void sendMessageGetChatId(Long chatId, String msg) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), msg);
        try {
            // Execute it
            telegramClient.execute(sendMessage);
            log.info("Отправлено сообщение: " + msg);
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

