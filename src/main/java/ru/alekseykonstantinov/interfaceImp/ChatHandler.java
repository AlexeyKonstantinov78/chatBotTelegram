package ru.alekseykonstantinov.interfaceImp;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ChatHandler {
    void handleUpdate(Update update);
}
