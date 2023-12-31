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
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final SubscriberRepository subscriberRepository;
    private final GetPriceCommand getPriceCommand;

    private String message = "";

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        Long sum = getSum(arguments);

        if (sum != null) {
            updateSubscribe(sum, message.getChatId());
            getPriceCommand.processMessage(absSender, message, arguments);
        }

        if (this.message == null || this.message.isEmpty()) {
            this.message = "Что-то пошло не так, извините(";
            log.error("Возникла ошибка при обработке создания подписки, " +
                    "не было сформированно сообщение для пользователя c id: {}", message.getChatId());
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        answer.setText(this.message);
        Sender.sendMessage(absSender, answer, getCommandIdentifier());
    }

    private void updateSubscribe(Long sum, Long chatId) {

        Optional<Subscriber> optional = subscriberRepository.findByUserId(chatId);
        Subscriber subscriber = optional.orElse(new Subscriber(chatId));
        subscriber.setPrice(sum);

        subscriberRepository.save(subscriber);
        this.message = "Создана новая подписка на стоимость " + sum + " USD" + System.lineSeparator() + System.lineSeparator()
        + "Когда цена достигнет данного значения вам будут приходить уведомления каждые 10 минут. " +
                "Для отключения уведомлений выполните команду /unsubscribe";
    }

    private Long getSum(String[] arguments) {

        if (arguments.length == 0) {
            this.message = "Не указана стоимость биткоина для подписки";
            return null;
        }

        String regexNum = "[0-9]{3,6}";

        if (arguments[0] == null || !arguments[0].matches(regexNum)) {
            this.message = "Указано некорректное число";
            return null;
        }

        try {
            return Long.valueOf(arguments[0]);
        } catch (Exception e) {
            this.message = "Число не было обработанно, попробуйте еще раз";
            return null;
        }
    }
}