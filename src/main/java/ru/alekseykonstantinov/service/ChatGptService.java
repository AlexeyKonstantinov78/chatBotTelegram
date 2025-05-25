package ru.alekseykonstantinov.service;

import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatGptService {
    private ChatGPT chatGPT;
    private final String apiHost = "https://api.openai.com/";
    private List<Message> messageHistory = new ArrayList<>(); //История переписки с ChatGPT - нужна для диалогов

    public ChatGptService(String TOKEN) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("18.199.183.77", 49232));
        this.chatGPT = ChatGPT.builder()
                .apiKey(TOKEN)
                .apiHost(apiHost)
                .proxy(proxy)
                .build()
                .init();
    }

    /**
     * Одиночный запрос к ChatGPT по формату "запрос"-> "ответ".
     * Запрос состоит из двух частей:
     * prompt - контектс вопроса
     * question - сам запрос
     */
    public String sendMessage(String prompt, String question) throws Exception {
        Message system = Message.ofSystem(prompt);
        Message message = Message.of(question);
        messageHistory = new ArrayList<>(Arrays.asList(system, message));

        return sendMessagesToChatGPT();
    }

    /**
     * Запросы к ChatGPT с сохранением истории сообщений.
     * Метод setPrompt() задает контект запроса
     */
    public void setPrompt(String prompt) {
        Message system = Message.ofSystem(prompt);
        messageHistory = new ArrayList<>(List.of(system));
    }

    /**
     * Запросы к ChatGPT с сохранением истории сообщений.
     * Метод addMessage() добавляет новый вопрос (сообщение) в чат.
     */
    public String addMessage(String question) throws Exception {
        Message message = Message.of(question);
        messageHistory.add(message);

        return sendMessagesToChatGPT();
    }

    /**
     * Отправляем ChatGPT серию сообщений: prompt, message1, answer1, message2, answer2, ..., messageN
     * Ответ ChatGPT добавляется в конец messageHistory для последующейго использования
     */
    private String sendMessagesToChatGPT() throws Exception {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(ChatCompletion.Model.GPT4o) // GPT4oMini or GPT_3_5_TURBO
                .messages(messageHistory)
                .maxTokens(3000)
                .temperature(0.9)
                .build();

        ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);
        Message res = response.getChoices().get(0).getMessage();
        messageHistory.add(res);

        return res.getContent();
    }
}
