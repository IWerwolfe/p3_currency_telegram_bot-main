package com.skillbox.cryptobot.bot;    /*
 *created by WerWolfe on Sender
 */

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class Sender {

    public static void sendMessage(AbsSender absSender, SendMessage message, String method) {

        try {
            absSender.execute(message);
        } catch (Exception e) {
            log.error("Ошибка возникла при выполнении {} метода", method, e);
        }
    }

}
