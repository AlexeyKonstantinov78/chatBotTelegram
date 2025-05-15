package ru.alekseykonstantinov;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_TOKEN;

@Slf4j
public class Main {
    public static void main(String[] args) {

        try {
            // Instantiate Telegram Bots API
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            // TODO Register our bot
            botsApplication.registerBot(TELEGRAM_BOT_TOKEN, new MyBotTelegram(TELEGRAM_BOT_TOKEN));
        } catch (TelegramApiException e) {
            log.error("TelegramApiException: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }
    }
}