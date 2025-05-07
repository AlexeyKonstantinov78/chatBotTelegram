package ru.alekseykonstantinov.telegrambot.group;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import java.util.List;

import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_GROUP_FRONT_NAME;
import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_TOKEN;

@Slf4j
public class WebFrontGroup extends MyBotTelegram {

    public WebFrontGroup() {
        super(TELEGRAM_BOT_TOKEN);
    }

    public void consumeGroup(Update update) {
        if (update.getMessage().hasEntities() && update.getMessage().getEntities().getFirst().getType().equals("bot_command")
                && update.getMessage().getEntities().getFirst().getText().equals("/list")) {
            log.info("Вызов списка лист");

            List<ChatMember> administrators = getChatAdministrators(update);
            log.info("Список админов: \n" + toPrettyJson(administrators));
            log.info("Count: " + getChatMemberCount(update));
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение в группе: " + TELEGRAM_BOT_GROUP_FRONT_NAME + " message:  " + message);
            Long chatId = update.getMessage().getChatId();
            sendMessageGetChatId(chatId, message);
        }

    }

    /**
     * Получение списка Администраторов и владельца группы
     *
     * @param update данные при событии
     */
    public List<ChatMember> getChatAdministrators(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        log.info("chatId group: " + chatId);
        List<ChatMember> administrators;

        try {
            GetChatAdministrators getChatAdministrators = new GetChatAdministrators(chatId.toString());
            administrators = telegramClient.execute(getChatAdministrators);

        } catch (TelegramApiException e) {
            log.info("Ошибка получения данных" + e.getMessage());
            return null;
        }
        return administrators;
    }

    public Integer getChatMemberCount(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        Integer chatMemberCount;

        try {
            GetChatMemberCount getChatMemberCount = new GetChatMemberCount(chatId.toString());
            chatMemberCount = telegramClient.execute(getChatMemberCount);

        } catch (TelegramApiException e) {
            log.info("Ошибка получения данных" + e.getMessage());
            return null;
        }
        return chatMemberCount;
    }

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
