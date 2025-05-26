package ru.alekseykonstantinov.enums;

import lombok.Getter;

@Getter
public enum YandexGPTtype {
    YANDEX_LITE("yandexgpt-lite"),
    YANDEX_RC("yandexgpt/rc"),
    YANDEX_LITE_RC("yandexgpt-lite/rc"),
    YANDEX_LATEST("yandexgpt/latest");


    private final String name;

    YandexGPTtype(String name) {
        this.name = name;
    }
}
