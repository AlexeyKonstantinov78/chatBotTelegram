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
    public static String GOOGLE_CLOUD_TRANSLIT_PROJECT_ID;
    public static String IAM_TOKEN;
    public static String FOLDER_ID;
    public static String GIGA_CHAT_KEY;
    public static String GIGA_CHAT_CLIENT_ID;
    public static String GIGA_CHAT_SECRET;

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
            IAM_TOKEN = props.getProperty("yandex.cloud.apiKey");
            FOLDER_ID = props.getProperty("yandex.cloud.folderId");
            GOOGLE_CLOUD_TRANSLIT_PROJECT_ID = props.getProperty("google.cloud.translit.projectId");
            GIGA_CHAT_KEY = props.getProperty("gigachat.api.pers");
            GIGA_CHAT_CLIENT_ID = props.getProperty("gigachat.api.pers.id.client");
            GIGA_CHAT_SECRET = props.getProperty("gigachat.api.pers.id.secret");

        } catch (IOException e) {
            log.error("Ошибка загрузки config.properties IOException: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Ошибка загрузки config.properties RuntimeException: {}", e.getMessage());
        }
    }
}

