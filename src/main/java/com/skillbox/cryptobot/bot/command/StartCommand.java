package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;


/**
 * Обработка команды начала работы с ботом
 */
@Service
@AllArgsConstructor
@Slf4j
public class StartCommand implements IBotCommand {
    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        checkAndCreateNewUser(message);

        String text = """
                Привет! 
                Данный бот помогает отслеживать стоимость биткоина.
                                
                Поддерживаемые команды:
                 /get_price - получить текущую стоимость биткоина
                 /subscribe XXXXX - подписаться на эту стоимость биткоина
                 /get_subscription - информация о подписке
                 /unsubscribe - отменить подписку
                """;

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        answer.setText(text);
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }

    private void checkAndCreateNewUser(Message message) {

        Long userId = message.getChatId();
        Optional<Subscriber> optional = subscriberRepository.findByUserId(userId);

        if (optional.isEmpty()) {
            Subscriber subscriber = optional.orElse(new Subscriber(userId));
            subscriberRepository.save(subscriber);
            log.info("Create at new user telegram's bot - {}", message.getFrom().getUserName());
        }
    }
}