package org.example.biobot.Consumers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final String text;

    public UpdateConsumer(@Value("${telegram.bot.token}") String token,
                          @Value("classpath:Bio_text.txt") Resource textResource) {
        this.telegramClient = new OkHttpTelegramClient(token);

        String loadtxt;
        try (InputStreamReader inputStreamReader = new InputStreamReader(textResource.getInputStream(), StandardCharsets.UTF_8)) {
            loadtxt = FileCopyUtils.copyToString(inputStreamReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.text = loadtxt;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallBackQuery(update);
        }
    }

    private void handleCallBackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();

        switch (callback) {
            case "my_data" -> sendMyBio(chatId);
            default -> sendMessage(chatId, "Неизвестный callback");
        }

    }

    private void sendMyBio(Long chatId) {
        sendMessage(chatId, text);
    }

    private void handleMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        switch (text) {
            case "/start" -> sendMainMenu(chatId);
            case "/help" -> sendHelpMessage(chatId);
            default -> sendUnknownMessage(chatId);
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage
                .builder()
                .text(text)
                .chatId(chatId)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendUnknownMessage(Long chatId) {
        sendMessage(chatId, "Ничего не понял. Введи /help для информации");
    }

    private void sendHelpMessage(Long chatId) {
        sendMessage(chatId, """
                Помощь по боту
                /start - Главное меню
                /help - Эта справка
                
                Используйте кнопки для навигации по функциям бота.
                """);
    }

    private void sendMainMenu(Long chatId) {
        SendMessage message = SendMessage
                .builder()
                .text("Привет!\nНажми кнопку для получения био👇")
                .chatId(chatId)
                .build();

        makeButton(message);

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void makeButton(SendMessage message) {
        var button1 = InlineKeyboardButton
                .builder()
                .text("Мое био")
                .callbackData("my_data")
                .build();

        List<InlineKeyboardRow> buttons = List.of(
                new InlineKeyboardRow(button1)
        );

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);

        message.setReplyMarkup(inlineKeyboardMarkup);
    }
}


