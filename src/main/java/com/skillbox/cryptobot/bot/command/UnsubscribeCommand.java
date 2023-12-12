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

/**
 * Обработка команды отмены подписки на курс валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UnsubscribeCommand implements IBotCommand {

    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "unsubscribe";
    }

    @Override
    public String getDescription() {
        return "Отменяет подписку пользователя";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        delSubscribe(message.getChatId());

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        answer.setText("Подписка отменена");
        Sender.sendMessage(absSender, answer, getCommandIdentifier());
    }

    private void delSubscribe(Long chatId) {

        Optional<Subscriber> optional = subscriberRepository.findByUserId(chatId);

        if (optional.isPresent()) {
            Subscriber subscriber = optional.get();
            subscriber.setPrice(null);
            subscriberRepository.save(subscriber);
        }
    }
}