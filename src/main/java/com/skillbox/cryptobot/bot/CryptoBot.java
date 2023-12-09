package com.skillbox.cryptobot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;

    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList
    ) {

        super(botToken);
        this.botUsername = botUsername;

        commandList.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {

        String text = """
                Я не умею общаться в свободном стиле, лучше отправь мне команду из этого списка:
                                
                /get_price - получить текущую стоимость биткоина
                 /subscribe XXXXX - подписаться на эту стоимость биткоина
                 /get_subscription - информация о подписке
                 /unsubscribe - отменить подписку
                """;

        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(text);
            Long userId = update.getMessage().getChatId();
            sendMessage.setChatId(userId);
            execute(sendMessage);

        } catch (TelegramApiException e) {
            log.error("Ошибка возникла при отправке уведомления пользователю", e);
        }
    }
}
