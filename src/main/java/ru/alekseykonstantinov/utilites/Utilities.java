package ru.alekseykonstantinov.utilites;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Utilities {
    public static final List<String> messageGreeting =
            Arrays.asList("Привет", "Hello", "Хай", "Салют", "Добрый", "Доброе", "Приветствую", "Здорова",
                    "Приветик", "Дарова", "Хэлло", "✋🏻", "🖖🏻", "Дратути", "Здравствуйте", "Доброго времени суток");
    public static final List<String> messageFormsOfFarewell =
            Arrays.asList("Пока", "до свидания", "до встречи", "до вечера", "до завтра", "Всего доброго", "Всего хорошего",
                    "Бывай", "Чао", "Покеда", "Увидимся", "Разрешите попрощаться", "Счастливо", "Всё пока", "Адьёс", "Бай",
                    "До связи", "До скорого", "Бегу", "Хорошего дня", "Спокойной ночи", "спокойный ночи", "Доброй ночи");

    public static final List<String> messageCompliments = Arrays.asList(
            "Ты выглядишь потрясающе сегодня",
            "У тебя отличное чувство юмора",
            "Ты очень умный и эрудированный",
            "С тобой приятно общаться",
            "Ты вдохновляешь окружающих",
            "У тебя прекрасный вкус",
            "Ты очень талантливый человек",
            "Ты излучаешь позитив",
            "Ты отлично справляешься со своими задачами",
            "У тебя charming улыбка",
            "Ты замечательный друг",
            "Ты прекрасно разбираешься в этом вопросе",
            "Ты очень креативный",
            "С тобой комфортно находиться рядом",
            "Ты делаешь мир лучше",
            "Ты прекрасна",
            "Ты очень умный",
            "Отличный стиль",
            "Классная улыбка",
            "Ты обаятельный",
            "Ты сияешь",
            "Ты молодец",
            "Отличная работа",
            "Ты вдохновляешь",
            "Ты харизматичный",
            "Ты очень добрый",
            "Ты талантлив",
            "Ты потрясающий",
            "Ты очаровательна",
            "Ты классный собеседник",
            "У тебя отличный вкус",
            "Ты быстро соображаешь",
            "Ты очень милая",
            "Ты выглядишь сногсшибательно",
            "Ты просто супер",
            "Ты умничка",
            "Ты умный",
            "Красавчик"
    );

    public static final List<String> expressionGratitude = Arrays.asList(
            "Благодарю",
            "Спасибо",
            "Пожалуйста"
    );

    public static String getRandomExpressionGratitude() {
        return expressionGratitude.get(ThreadLocalRandom.current().nextInt(0, expressionGratitude.size()));
    }

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
        //log.info(arrMessage.toString());
        return arrMessage.stream().anyMatch(mess -> list.stream().anyMatch(mess::equalsIgnoreCase));
    }

    /**
     * Проверяет частичное совпадение с массивом первое вхождение возвращает true
     *
     * @param message сообщение
     * @param list    список слов
     * @return Boolean
     */
    public static Boolean getIsMessageArraysForms(String message, List<String> list) {
        return list.stream()
                .anyMatch(word -> message.toLowerCase().contains(word.toLowerCase()));
    }

}
