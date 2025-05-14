package ru.alekseykonstantinov.telegrambot.group;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import java.util.List;

import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_GROUP_FRONT_NAME;
import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_TOKEN;
import static ru.alekseykonstantinov.utilites.Utilities.toPrettyJson;

@Slf4j
public class WebFrontGroup extends MyBotTelegram {

    public WebFrontGroup() {
        super(TELEGRAM_BOT_TOKEN);
    }

    public void consumeGroup(Update update) {
        //ChatActions, такие как «набор текста» или «запись голосового сообщения»
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatActions(update);
        }

        if (update.hasMessage()) {
            if (update.getMessage().getFrom() != null) {
                ChatMember chatMember = getChatMember(update);
                log.info("Информация о member: {}", toPrettyJson(chatMember));
                log.info("Роль в группе: {}", getMemberRole(chatMember));
            }
            if (update.getMessage().getChat() != null) {
                Long chatId = update.getMessage().getChatId();
                ChatFullInfo chatFullInfo = getChat(chatId);
                log.info("Информация о чате: \n{}", toPrettyJson(chatFullInfo));
            }
        }

        if (update.getMessage().hasEntities() && update.getMessage().getEntities().getFirst().getType().equals("bot_command")
                && update.getMessage().getEntities().getFirst().getText().equals("/list")) {
            log.info("Вызов списка лист");

            List<ChatMember> administrators = getChatAdministrators(update);
            log.info("Список админов: \n{}", toPrettyJson(administrators));
            log.info("Count: {}", getChatMemberCount(update));

            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение в группе: {}{}{}", TELEGRAM_BOT_GROUP_FRONT_NAME, " message:  ", message);
            Long chatId = update.getMessage().getChatId();
            sendMessageGetChatId(chatId, message);

            //отправка изображения по названию
            sendImageUploadingAFileJpg("ulybashka", chatId.toString());

        }

    }

    /**
     * Получение данных о чате
     *
     * @param chatId ид чата
     * @return class ChatFullInfo
     */
    public ChatFullInfo getChat(Long chatId) {
        GetChat getChat = new GetChat(chatId.toString());
        ChatFullInfo infoChat;
        try {
            infoChat = telegramClient.execute(getChat);
        } catch (TelegramApiException e) {
            log.error("Не получили информацию о чате: {}", e.getMessage());
            return null;
        }
        return infoChat;
    }

    /**
     * Получение списка Администраторов и владельца группы
     *
     * @param update данные при событии
     */
    public List<ChatMember> getChatAdministrators(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        List<ChatMember> administrators;

        try {
            GetChatAdministrators getChatAdministrators = new GetChatAdministrators(chatId.toString());
            administrators = telegramClient.execute(getChatAdministrators);

        } catch (TelegramApiException e) {
            log.error("Ошибка получения списка админов данных: {}", e.getMessage());
            return null;
        }
        return administrators;
    }

    /**
     * Получает данные member из группы которые разрешены
     *
     * @param update событие
     * @return class ChatMember
     */
    public ChatMember getChatMember(Update update) {
        ChatMember chatMemberUser;
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        GetChatMember getChatMember = new GetChatMember(chatId.toString(), userId);
        try {
            chatMemberUser = telegramClient.execute(getChatMember);
        } catch (TelegramApiException e) {
            log.error("Ошибка получения данных члена: {}", e.getMessage());
            return null;
        }

        return chatMemberUser;
    }

    /**
     * Получаем количество участников группы
     *
     * @param update событие данные
     * @return количество members
     */
    public Integer getChatMemberCount(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        Integer chatMemberCount;

        try {
            GetChatMemberCount getChatMemberCount = new GetChatMemberCount(chatId.toString());
            chatMemberCount = telegramClient.execute(getChatMemberCount);

        } catch (TelegramApiException e) {
            log.error("Ошибка получения количества членов группы: {}", e.getMessage());
            return null;
        }
        return chatMemberCount;
    }

    /**
     * Роли группы участников
     *
     * @param member данные участника
     * @return тип участника
     */
    private String getMemberRole(ChatMember member) {
        if (member instanceof ChatMemberOwner) {
            return "Владелец";
        } else if (member instanceof ChatMemberAdministrator) {
            return "Администратор";
        } else {
            return "Участник";
        }
    }

}
