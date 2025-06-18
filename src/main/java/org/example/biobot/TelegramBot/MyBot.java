package org.example.biobot.TelegramBot;


import org.example.biobot.Consumers.UpdateConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class MyBot implements SpringLongPollingBot {

    @Value("${telegram.bot.token}")
    String token;

    private final UpdateConsumer updateConsumer;

    public MyBot(UpdateConsumer updateConsumer) {
        this.updateConsumer = updateConsumer;
    }


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
