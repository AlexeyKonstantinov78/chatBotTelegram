package ru.alekseykonstantinov.telegrambot.group;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.ChatFullInfo;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import ru.alekseykonstantinov.interfaceImp.ChatHandler;
import ru.alekseykonstantinov.telegrambot.MyBotTelegram;

import java.util.List;

import static ru.alekseykonstantinov.config.Config.TELEGRAM_BOT_GROUP_FRONT_NAME;
import static ru.alekseykonstantinov.utilites.Utilities.toPrettyJson;

@Slf4j
public class WebFrontGroup implements ChatHandler {
    private final MyBotTelegram bot;


    public WebFrontGroup(MyBotTelegram bot) {
        this.bot = bot;
    }

    @Override
    public void handleUpdate(Update update) {
//        //ChatActions, такие как «набор текста» или «запись голосового сообщения»
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            chatActions(update);
//        }

        if (update.hasMessage()) {
            if (update.getMessage().getFrom() != null) {
                ChatMember chatMember = bot.getChatMember(update);
                log.info("Информация о member: {}", toPrettyJson(chatMember));
                log.info("Роль в группе: {}", bot.getMemberRole(chatMember));
            }
            if (update.getMessage().getChat() != null) {
                Long chatId = update.getMessage().getChatId();
                ChatFullInfo chatFullInfo = bot.getChat(chatId);
                log.info("Информация о чате: \n{}", toPrettyJson(chatFullInfo));
            }
        }

        if (update.getMessage().hasEntities() && update.getMessage().getEntities().getFirst().getType().equals("bot_command")
                && update.getMessage().getEntities().getFirst().getText().equals("/list")) {
            log.info("Вызов списка лист");

            List<ChatMember> administrators = bot.getChatAdministrators(update);
            log.info("Список админов: \n{}", toPrettyJson(administrators));
            log.info("Count: {}", bot.getChatMemberCount(update));

            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            log.info("Получено сообщение в группе: {}{}{}", TELEGRAM_BOT_GROUP_FRONT_NAME, " message:  ", message);
            Long chatId = bot.getChatId(update);
            bot.sendMessageGetChatId(chatId, message);

            //отправка изображения по названию
            if (message.equalsIgnoreCase("привет")
                    || message.equalsIgnoreCase("hello")) {
                bot.sendImageUploadingAFileJpg("ulybashka", chatId);
            }
        }
    }

}
