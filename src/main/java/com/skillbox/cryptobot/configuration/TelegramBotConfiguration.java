package com.skillbox.cryptobot.configuration;


import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class TelegramBotConfiguration {

    private final long delay = 2;
    private final long notifyRange = 10;
    private final CryptoCurrencyService service;
    private final SubscriberRepository subscriberRepository;
    private CryptoBot cryptoBot;

    public TelegramBotConfiguration(CryptoCurrencyService service, SubscriberRepository subscriberRepository) {
        this.service = service;
        this.subscriberRepository = subscriberRepository;
    }

    @Bean
    TelegramBotsApi telegramBotsApi(CryptoBot cryptoBot) {
        TelegramBotsApi botsApi = null;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(cryptoBot);

            this.cryptoBot = cryptoBot;
            startSchedule();

        } catch (TelegramApiException e) {
            log.error("Error occurred while sending message to telegram!", e);
        }
        return botsApi;
    }

    private void startSchedule() {

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::checkSubscribes
                , delay * 2
                , delay
                , TimeUnit.MINUTES);
    }

    private void checkSubscribes() {

        try {
            double sum = service.getBitcoinPrice();
            LocalDateTime date = LocalDateTime.now().minusMinutes(notifyRange);
            SendMessage message = getSendMessage(sum);

            List<Subscriber> subscriberList = subscriberRepository.findByPriceNotNullAndPriceGreaterThanEqualAndDateNotificationNullOrDateNotificationLessThan((long) sum, date);

            subscriberList.forEach(subscriber -> {
                message.setChatId(subscriber.getUserId());
                sendMessage(message);
                subscriber.setDateNotification(LocalDateTime.now());
                subscriberRepository.save(subscriber);
            });

        } catch (Exception e) {
            log.error("Ошибка возникла при формировании уведомлений для пользователей", e);
        }
    }

    private SendMessage getSendMessage(double sum) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Пора покупать, стоимость биткоина " + TextUtil.toString(sum) + " USD");
        return sendMessage;
    }

    private void sendMessage(SendMessage message) {

        try {
            cryptoBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка возникла при отправке уведомления пользователю {}", message.getChatId(), e);
        }
    }
}
