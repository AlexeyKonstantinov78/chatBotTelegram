package ru.alekseykonstantinov.config;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//@Slf4j
@Getter
public class Config {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Config.class);
    private static String TELEGRAM_BOT_NAME; //TODO: добавь имя бота в кавычках
    private static String TELEGRAM_BOT_TOKEN; //TODO: добавь токен бота в кавычках

    public Config() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            props.load(input); // Загружаем файл

            // Получаем значения
            TELEGRAM_BOT_NAME = props.getProperty("telegram.bot.name");
            TELEGRAM_BOT_TOKEN = props.getProperty("telegram.bot.token");

        } catch (IOException e) {

            log.error("Ошибка загрузки config.properties: " + e.getMessage());
        }

    }
}

