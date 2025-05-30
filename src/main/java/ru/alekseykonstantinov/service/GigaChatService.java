package ru.alekseykonstantinov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import ru.alekseykonstantinov.utilites.UnsafeHttpClient;

import java.io.IOException;

import static ru.alekseykonstantinov.config.Config.*;

public class GigaChatService {
    private final String gigaChatAuthUrl = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
    private final String gigaChatApiUrl = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
    private final String clientId = GIGA_CHAT_CLIENT_ID;
    private final String clientSecret = GIGA_CHAT_SECRET;
    private String accessToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GigaChatService() {
        this.accessToken = getAccessToken();
    }

    private String getAccessToken() {
        try (CloseableHttpClient httpClient = UnsafeHttpClient.create()) {
            HttpPost httpPost = new HttpPost(gigaChatAuthUrl);

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Authorization", "Basic " + GIGA_CHAT_KEY);
            httpPost.setHeader("RqUID", "5114e616-79fc-4fe9-bd54-236aca62f61f");

            String authRequest = String.format(
                    "scope=GIGACHAT_API_PERS&client_id=%s&client_secret=%s",
                    clientId, clientSecret
            );

            httpPost.setEntity(new StringEntity(authRequest));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity);
                JSONObject jsonResponse = new JSONObject(responseString);
                return jsonResponse.getString("access_token");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getGigaChatResponse(String userMessage) {
        String prompt = "Вести разговор от имени Alex AI Bot. Чат бот. темы разные";
        if (accessToken == null) {
            return "Ошибка аутентификации с GigaChat API";
        }

        try (CloseableHttpClient httpClient = UnsafeHttpClient.create()) {
            HttpPost httpPost = new HttpPost(gigaChatApiUrl);
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            httpPost.setHeader("Content-Type", "application/json");

            String requestBody = String.format(
                    "{\"model\":\"GigaChat-2\"," +
                            "\"messages\":[" +
                            "{\"role\":\"system\",\"content\":\"%s\"}" +
                            ",{\"role\":\"user\",\"content\":\"%s\"}]}",
                    prompt,
                    userMessage.replace("\"", "\\\"")
            );

            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity);
                JSONObject jsonResponse = new JSONObject(responseString);
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Произошла ошибка при обращении к GigaChat API";
        }
    }
}
