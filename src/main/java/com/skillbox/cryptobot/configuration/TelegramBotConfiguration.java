package com.skillbox.cryptobot.configuration;


import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.bot.Sender;
import com.skillbox.cryptobot.model.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TelegramBotConfiguration {

    private final long delay = 2;
    private final long notifyRange = 10;

    private final CryptoCurrencyService service;
    private final SubscriberRepository subscriberRepository;
    private CryptoBot cryptoBot;

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

        double sum = getSum();

        if (sum <= 0) {
            return;
        }

        LocalDateTime date = LocalDateTime.now().minusMinutes(notifyRange);
        SendMessage message = getSendMessage(sum);

        List<Subscriber> subscriberList = subscriberRepository.findActiveSubscribers((long) sum, date);

        subscriberList.forEach(subscriber -> {
            message.setChatId(subscriber.getUserId());
            Sender.sendMessage(cryptoBot, message, "sendNotification");
            updateSubscribe(subscriber);
        });
    }

    private double getSum() {

        double sum;
        try {
            sum = service.getBitcoinPrice();
        } catch (IOException e) {
            log.error("Возникла ошибка при получении цены биткоина", e);
            throw new RuntimeException(e);
        }
        return sum;
    }

    private void updateSubscribe(Subscriber subscriber) {
        subscriber.setDateNotification(LocalDateTime.now());
        subscriberRepository.save(subscriber);
    }

    private SendMessage getSendMessage(double sum) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Пора покупать, стоимость биткоина " + TextUtil.toString(sum) + " USD");
        return sendMessage;
    }
}
