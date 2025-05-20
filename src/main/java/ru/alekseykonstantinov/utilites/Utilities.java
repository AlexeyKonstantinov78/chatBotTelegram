package ru.alekseykonstantinov.utilites;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class Utilities {
    public static final List<String> MessageGreeting =
            Arrays.asList("Привет", "Hello", "Хай", "Салют", "Добрый", "Доброе", "Приветствую", "Здорово", "Приветик", "Дарова", "Хэлло", "✋🏻", "🖖🏻", "Дратути", "Здравствуйте");

    /**
     * @param user юзер
     *             Метод для получения данных юзера типа toString()
     */
    public static String getUserData(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("id: {}" + user.getId() + " ");
        sb.append("firstName: " + user.getFirstName() + " ");
        sb.append("isBot: " + user.getIsBot() + " ");
        sb.append("userName: " + user.getUserName() + " ");

        return sb.toString();
    }

    /**
     * Разбор ответа на json формат
     *
     * @param update список события
     * @return возврат в json формате
     */
    public static <T> String toPrettyJson(T update) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(update);
    }

    public static Boolean getIsMessageArrays(String message, List<String> list) {
        List<String> arrMessage = List.of(message.split("\\s+"));
        log.info(arrMessage.toString());
        return arrMessage.stream().anyMatch(mess -> list.stream().anyMatch(mess::equalsIgnoreCase));
    }
}
