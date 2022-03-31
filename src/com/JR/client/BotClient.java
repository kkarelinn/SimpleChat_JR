package com.JR.client;

import com.JR.Message;
import com.JR.MessageType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client {
    private static final String helloBot =
            "Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.";

    public static void main(String[] args) {

        BotClient bot = new BotClient();
        bot.run();
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }


    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage(helloBot);
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);

            if (message.contains(": ")) {
                String pattern = "";
                Calendar dateTime = Calendar.getInstance();
                int index = message.indexOf(":");
                String name = message.substring(0, index);
                String mess = message.substring(index + 1).trim();
                switch (mess) {
                    case ("дата"):
                        pattern = "d.MM.YYYY";
                        break;
                    case ("день"):
                        pattern = "d";
                        break;
                    case ("месяц"):
                        pattern = "MMMM";
                        break;
                    case ("год"):
                        pattern = "YYYY";
                        break;
                    case ("время"):
                        pattern = "H:mm:ss";
                        break;
                    case ("час"):
                        pattern = "H";
                        break;
                    case ("минуты"):
                        pattern = "m";
                        break;
                    case ("секунды"):
                        pattern = "s";
                        break;
                }
                if(!pattern.isEmpty()){
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    BotClient.this.sendTextMessage(String.format("Информация для %s: %s", name, sdf.format(dateTime.getTime())));
                }

            }

        }
    }
}

