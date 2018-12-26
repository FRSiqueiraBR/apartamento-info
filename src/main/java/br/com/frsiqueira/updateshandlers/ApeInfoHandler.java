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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
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

    private static final int STARTSTATE = 0;

    public ApeInfoHandler() {
        super();
        DatabaseManager.getInstance();
        startAlertTimers();
    }

    private static boolean isCommandForOther(String text) {
        boolean isSimpleCommand = text.equals("/start") || text.equals("/help") || text.equals("/stop");
        boolean isCommandForMe = text.equals("/start@nomebot") || text.equals("/help@nomebot") || text.equals("/stop@nomebot");
        return text.startsWith("/") && !isSimpleCommand && !isCommandForMe;
    }

    private static SendMessage messageOnMenu(Message message) {
        SendMessage sendMessage;

        if (message.hasText()) {
            if (message.getText().equals(getDaysRemainingCommand())) {
                sendMessage = onDaysRemainingChosen(message);
            } else if (message.getText().equals(getAlertPaymentCommand())) {
                sendMessage = onAlertPaymentChosen(message);
            } else {
                sendMessage = sendChooseOptionMessage(message.getChatId(), message.getMessageId(), getMainMenuKeyboard());
            }
        } else {
            sendMessage = sendChooseOptionMessage(message.getChatId(), message.getMessageId(), getMainMenuKeyboard());
        }

        return sendMessage;
    }

    private static String getDaysRemainingCommand() {
        return "Dias Restantes";
    }

    private static String getAlertPaymentCommand() {
        return "Me avise quando tenho que pagar";
    }

    private static ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getDaysRemainingCommand());
        keyboardFirstRow.add(getAlertPaymentCommand());
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private static SendMessage onDaysRemainingChosen(Message message) {
        Period period = remainingDays();

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(generateRemainingDaysToRelease(period));

        return sendMessage;
    }

    private static SendMessage onAlertPaymentChosen(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        //TODO: fazer a pesquisa da data de pagamento
        sendMessage.setText("O pagamento deverá ser feito no dia x");

        return sendMessage;
    }

    private static SendMessage sendChooseOptionMessage(Long chatId, Integer messageId, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId.toString());
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setReplyMarkup(replyKeyboard);
        sendMessage.setText("Opção não encontrada");

        return sendMessage;
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
        //final int state = DatabaseManager.getInstance().getApeInfoState(message.getFrom().getId(), message.getChatId());

        if (!message.isUserMessage() && message.hasText()) {
            if (isCommandForOther(message.getText())) {
                return;
            } else if (message.getText().startsWith(Commands.STOP)) {
                sendHideKeyboard(message.getFrom().getId(), message.getChatId(), message.getMessageId());
                return;
            }
        }

        SendMessage sendMessageRequest;

        sendMessageRequest = messageOnMenu(message);

        execute(sendMessageRequest);
    }

    private void sendHideKeyboard(Integer userId, Long chatId, Integer messageId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText("Colocar texto");

        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        execute(sendMessage);
        DatabaseManager.getInstance().insertApeInfoState(userId, chatId, STARTSTATE);
    }
}
