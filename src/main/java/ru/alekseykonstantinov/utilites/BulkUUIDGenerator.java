package ru.alekseykonstantinov.utilites;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BulkUUIDGenerator {
    private static String str = "";

    public static List<String> generateBulkUUIDs(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());
    }

    public static String getUUID() {
        generateBulkUUIDs(1).forEach(uuid -> {
            str += uuid;
        });

        return str;
    }
}
