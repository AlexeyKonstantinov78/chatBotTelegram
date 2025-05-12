package ru.alekseykonstantinov.utilites;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.telegram.telegrambots.meta.api.objects.User;

public class Utilities {

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

}
