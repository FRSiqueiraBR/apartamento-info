package br.com.frsiqueira.updateshandlers;

import br.com.frsiqueira.BotConfig;
import br.com.frsiqueira.Commands;
import br.com.frsiqueira.database.DatabaseManager;
import br.com.frsiqueira.dto.ApeInfoAlert;
import br.com.frsiqueira.services.CustomTimerTask;
import br.com.frsiqueira.services.TimerExecutor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ApeInfoHandler extends TelegramLongPollingBot {
    private static final String LOGTAG = "APEINFOHANDLER";

    public ApeInfoHandler() {
        super();
        getMainMenuKeyboard();
        startAlertTimers();
    }

    private static Period remainingDays() {
        Date releaseDate = DatabaseManager.getInstance().findReleaseDate();

        LocalDate today = LocalDate.now();
        LocalDate releaseLocalDate = Instant.ofEpochMilli(releaseDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return Period.between(today, releaseLocalDate);
    }

    private static String generateRemainingDaysToRelease(Period period) {
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        String message;

        if (days != 0 && months != 0 && years != 0) {
            message = "Faltam " + years + " anos, " + months + " meses e " + days + " dias";
        } else if (days != 0 && months != 0) {
            message = "Faltam " + months + " meses e " + days + " dias";
        } else if (days != 0) {
            message = "Faltam " + days + " dias";
        } else {
            message = "Hoje é a data de entrega!";
        }

        return message;
    }

    private static SendMessage onStartChosen(Message message) {
        try {
            Integer userId = message.getFrom().getId();
            Long chatId = message.getChatId();
            Date now = new Date();

            DatabaseManager
                    .getInstance()
                    .saveUser(userId, chatId, now);

        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }

        return new SendMessage()
                .enableMarkdown(true)
                .setReplyToMessageId(message.getMessageId())
                .setChatId(message.getChatId())
                .setReplyMarkup(getMainMenuKeyboard())
                .setText("Seja bem vindo ao bot de informações do apartamento");
    }

    private static void onStopChosen(Message message) {
        try {
            Integer userId = message.getFrom().getId();
            Long chatId = message.getChatId();

            DatabaseManager
                    .getInstance()
                    .removeUser(userId, chatId);
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    private static SendMessage onDaysRemainingChosen(Message message) {
        Period period = remainingDays();

        return new SendMessage()
                .enableMarkdown(true)
                .setReplyToMessageId(message.getMessageId())
                .setChatId(message.getChatId())
                .setReplyMarkup(getMainMenuKeyboard())
                .setText(generateRemainingDaysToRelease(period));
    }

    //TODO: fazer a pesquisa da data de pagamento
    private static SendMessage onAlertPaymentChosen(Message message) {
        return new SendMessage()
                .enableMarkdown(true)
                .setReplyToMessageId(message.getMessageId())
                .setChatId(message.getChatId())
                .setReplyMarkup(getMainMenuKeyboard())
                .setText("O pagamento deverá ser feito no dia x");
    }

    private static SendMessage onUnknownOptionChosen(Message message) {
        return new SendMessage()
                .enableMarkdown(true)
                .setReplyToMessageId(message.getMessageId())
                .setChatId(message.getChatId())
                .setReplyMarkup(getMainMenuKeyboard())
                .setText("Desculpe, opção não encontrada");
    }

    private static ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(Commands.DAYS_REMAINING);
        keyboardFirstRow.add(Commands.ALERT);
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText() || message.hasLocation()) {
                    handleIncomingMessage(message);
                }
            }
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public String getBotUsername() {
        return BotConfig.APE_INFO_BOT;
    }

    public String getBotToken() {
        return BotConfig.APE_INFO_TOKEN;
    }

    private void startAlertTimers() {
        TimerExecutor.getInstance().startExecutionEveryDayAt(new CustomTimerTask("First day alert", -1) {
            @Override
            public void execute() {
                sendAlerts();
            }
        }, 0, 0, 0);

        TimerExecutor.getInstance().startExecutionEveryDayAt(new CustomTimerTask("Second day alert", -1) {
            @Override
            public void execute() {
                sendAlerts();
            }
        }, 12, 0, 0);
    }

    private void sendAlerts() {
        List<ApeInfoAlert> allAlerts = Collections.singletonList(new ApeInfoAlert());//TODO: Fazer a pesquisa de todos os usuarios que fizeram o alerta
        for (ApeInfoAlert apeInfoAlert : allAlerts) {
            synchronized (Thread.currentThread()) {
                try {
                    Thread.currentThread().wait(35);
                } catch (InterruptedException e) {
                    BotLogger.severe(LOGTAG, e);
                }
            }
            String message = "Faltam 500 dias para a entrega do apartamento";
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(String.valueOf(apeInfoAlert.getUserId()));
            sendMessage.setText(message);
            try {
                execute(sendMessage);
            } catch (TelegramApiRequestException e) {
                BotLogger.warn(LOGTAG, e);
            } catch (Exception e) {
                BotLogger.severe(LOGTAG, e);
            }
        }
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        if (this.isStartMessage(message.getText())) {
            execute(onStartChosen(message));
        } else if (this.isStopMessage(message.getText())) {
            onStopChosen(message);
        } else if (this.isDaysRemainingMessage(message.getText())) {
            execute(onDaysRemainingChosen(message));
        } else if (this.isAlertPaymentMessage(message.getText())) {
            execute(onAlertPaymentChosen(message));
        } else {
            execute(onUnknownOptionChosen(message));
        }
    }

    private boolean isStartMessage(String message) {
        return Commands.START.equals(message);
    }

    private boolean isStopMessage(String message) {
        return Commands.STOP.equals(message);
    }

    private boolean isDaysRemainingMessage(String message) {
        return Commands.DAYS_REMAINING.equals(message);
    }

    private boolean isAlertPaymentMessage(String message) {
        return Commands.ALERT.equals(message);
    }
}
