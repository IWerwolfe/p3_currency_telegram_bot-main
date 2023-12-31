package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.bot.Sender;
import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {
    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        String text = getSubscribeUser(message.getChatId());

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        answer.setText(text);
        Sender.sendMessage(absSender, answer, getCommandIdentifier());
    }

    private String getSubscribeUser(Long chatId) {

        Optional<Subscriber> optional = subscriberRepository.findByUserId(chatId);

        if (optional.isEmpty()) {
            return "Активная подписка отсутствует";
        }

        Subscriber subscriber = optional.get();
        return subscriber.getPrice() == null ?
                "Активная подписка отсутствует" :
                "Вы подписанны на стоимость биткоина " + subscriber.getPrice() + " USD";
    }
}