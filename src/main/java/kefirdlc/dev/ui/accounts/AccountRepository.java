package kefirdlc.dev.ui.accounts;
// coded by sitoku \\
// since 28.04.2026 \\

import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AccountRepository {

    private static final String FILE_NAME = "kefir-accounts.txt";

    private AccountRepository() {
    }

    public static List<String> load() {
        Path path = getFilePath();
        if (!Files.exists(path)) return new ArrayList<>();

        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .distinct()
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void save(List<String> accounts) {
        Path path = getFilePath();
        try {
            Files.write(path, accounts, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static Path getFilePath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve(FILE_NAME);
    }
}
