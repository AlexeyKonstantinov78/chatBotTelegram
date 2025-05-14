package ru.alekseykonstantinov.telegrambot.privatechat;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_GROUP_FRONT_NAME;
import static ru.alekseykonstantinov.utilites.Utilities.toPrettyJson;

@Slf4j
public class PrivateChat extends MyBotTelegram {

    public PrivateChat(String TOKEN) {
        super(TOKEN);
    }

    public void consumePrivate(Update update) {
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

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение в группе: {}{}{}", TELEGRAM_BOT_GROUP_FRONT_NAME, " message:  ", message);
            Long chatId = update.getMessage().getChatId();
            sendMessageGetChatId(chatId, message);

            //отправка изображения по названию
            if (message.equalsIgnoreCase("привет")
                    || message.equalsIgnoreCase("hello")) {
                sendImageUploadingAFileJpg("ulybashka", chatId.toString());
            }

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
