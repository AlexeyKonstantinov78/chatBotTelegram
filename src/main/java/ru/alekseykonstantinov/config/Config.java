package ru.alekseykonstantinov.config;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//@Slf4j
@Getter
public class Config {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Config.class);
    public static String TELEGRAM_BOT_NAME; //TODO: добавь имя бота в кавычках
    public static String TELEGRAM_BOT_TOKEN; //TODO: добавь токен бота в кавычках
    public static String TELEGRAM_BOT_GROUP_FRONT_NAME;
    public static String OPENAI_API_KEY;
    public static String DIALOGFLOW_KEY;
    public static String GOOGLE_CLOUD_PROJECT_ID;


    static {
        Properties props = new Properties();
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(input); // Загружаем файл

            // Получаем значения
            TELEGRAM_BOT_NAME = props.getProperty("telegram.bot.name");
            TELEGRAM_BOT_TOKEN = props.getProperty("telegram.bot.token");
            TELEGRAM_BOT_GROUP_FRONT_NAME = props.getProperty("telegram.bot.group.name.front");
            OPENAI_API_KEY = props.getProperty("openai.api.key");
            DIALOGFLOW_KEY = props.getProperty("google.cloud.console");
            GOOGLE_CLOUD_PROJECT_ID = props.getProperty("google.cloud.projectId");

        } catch (IOException e) {
            log.error("Ошибка загрузки config.properties IOException: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Ошибка загрузки config.properties RuntimeException: {}", e.getMessage());
        }
    }
}

