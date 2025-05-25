package ru.alekseykonstantinov.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class DialogflowConnector {
    private final String projectId;
    private final String credentialsPath;

    public DialogflowConnector(String projectId, String credentialsPath) {
        this.projectId = projectId;
        this.credentialsPath = credentialsPath;
    }

    public String detectIntent(String sessionId, String text, String languageCode)
            throws IOException {
        // Настройка аутентификации
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsPath));


        SessionsSettings settings = SessionsSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        // Создание сессии
        try (SessionsClient sessionsClient = SessionsClient.create(settings)) {
            SessionName session = SessionName.of(projectId, sessionId);

            // Создание текстового ввода
            TextInput.Builder textInput = TextInput.newBuilder()
                    .setText(text)
                    .setLanguageCode(languageCode);

            QueryInput queryInput = QueryInput.newBuilder()
                    .setText(textInput)
                    .build();

            // Отправка запроса и получение ответа
            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

            // Обработка ответа
            QueryResult queryResult = response.getQueryResult();
            String outMess = queryResult.getFulfillmentText();
            log.info("Ответ DialogflowConnector: {}", outMess);
            return outMess;
        }
    }
}
