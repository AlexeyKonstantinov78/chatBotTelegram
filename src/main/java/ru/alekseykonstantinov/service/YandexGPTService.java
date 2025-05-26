package ru.alekseykonstantinov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ru.alekseykonstantinov.enums.YandexGPTtype;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class YandexGPTService {
    private static final String YANDEX_GPT_URL = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion";
    private final String iamToken;
    private final String folderId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public YandexGPTService(String iamToken, String folderId) {
        this.iamToken = iamToken;
        this.folderId = folderId;
    }

    public String generateText(String prompt) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(YANDEX_GPT_URL);

            // Установка заголовков
            request.setHeader("Authorization", "Bearer " + iamToken);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("x-folder-id", folderId);

            // Создание тела запроса
            Map<String, Object> requestBody = new HashMap<>();
            ;
            requestBody.put("modelUri", "gpt://" + folderId + "/" + YandexGPTtype.YANDEX_LITE.getName());
            requestBody.put("completionOptions", Map.of(
                    "temperature", 0.3,
                    "maxTokens", 500,
                    "reasoningOptions.mode", "ENABLED_HIDDEN"
            ));

            Map<String, String> system = new HashMap<>();
            system.put("role", "system");
            system.put("text", "Вести разговор от имени Alex AI Bot");

            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("text", prompt);
            requestBody.put("messages", new Map[]{system, message});

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            request.setEntity(new StringEntity(requestBodyJson));

            log.info(requestBodyJson);

            // Выполнение запроса
            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            log.info("YandexGPT: {}", responseBody);

            // Парсинг ответа
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            Map<String, Object> result = (Map<String, Object>) responseMap.get("result");
            Map<String, Object> alternatives = (Map<String, Object>) ((java.util.List<?>) result.get("alternatives")).get(0);
            Map<String, String> mess = (Map<String, String>) alternatives.get("message");
            String out = mess.get("text");
            log.info("Ответ YandexGPT: {}", out);

            return out;
        }
    }
}


