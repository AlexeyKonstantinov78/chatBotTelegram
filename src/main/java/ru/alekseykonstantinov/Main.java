package ru.alekseykonstantinov;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alekseykonstantinov.config.Config;

@Slf4j
public class Main {
    private static String TOKEN;
    private static String NAME;

    static {
        Config config = new Config();
        TOKEN = config.getTELEGRAM_BOT_TOKEN();
        NAME = config.getTELEGRAM_BOT_NAME();
    }

    public static void main(String[] args) {

        try {
            // Instantiate Telegram Bots API
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            // TODO Register our bot
            botsApplication.registerBot(TOKEN, new MyBot(TOKEN));
        } catch (TelegramApiException e) {
            log.error("TelegramApiException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}