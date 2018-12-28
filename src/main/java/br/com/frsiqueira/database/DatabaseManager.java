package br.com.frsiqueira.database;

import br.com.frsiqueira.dto.ApeInfoAlert;
import br.com.frsiqueira.dto.Payment;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseManager {
    private static final String LOGTAG = "DATABASEMANAGER";

    private static volatile DatabaseManager instance;
    private static volatile ConnectionDB connection;

    public DatabaseManager() {
        connection = new ConnectionDB();
        this.recreateTables();
    }

    public static DatabaseManager getInstance() {
        final DatabaseManager currentInstance;

        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    private void recreateTables() {
        try {
            connection.initTransaction();

            connection.executeQuery(CreationString.createUserTable);
            connection.executeQuery(CreationString.createApartmentTable);
            connection.executeQuery(CreationString.createPaymentTable);
            connection.executeQuery(CreationString.createAlertTable);

            connection.executeQuery(CreationString.deleteApartmentTable);
            connection.executeQuery(CreationString.deletePaymentTable);

            connection.executeQuery(CreationString.insertApartmentTable);
            connection.executeQuery(CreationString.insertPaymentTable);


            connection.commitTransaction();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public Date findReleaseDate() {
        Date releaseDate = null;
        try {

            final PreparedStatement preparedStatement = connection.getPreparedStatement("SELECT * FROM APARTMENT");
            final ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                releaseDate = result.getDate("release_date");
            }


        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
        return releaseDate;
    }

    public void saveUser(Integer userId, Long chatId, Date creationDate) {
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("REPLACE INTO USER(user_id, chat_id, create_date) VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userId);
            preparedStatement.setLong(2, chatId);
            preparedStatement.setDate(3, new java.sql.Date(creationDate.getTime()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public void removeUser(Integer userId, Long chatId) {
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("DELETE FROM USER WHERE user_id = ? and chat_id = ?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public List<Payment> findPayments() {
        List<Payment> payments = new ArrayList<>();

        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("SELECT ID, PARCEL, DATE, TYPE, AMOUNT, PAID FROM PAYMENT");

            final ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                Payment payment = new Payment();
                payment.setId(result.getInt("id"));
                payment.setParcel(result.getInt("parcel"));
                payment.setDate(result.getDate("date"));
                payment.setType(result.getString("type"));
                payment.setAmount(result.getBigDecimal("amount"));
                payment.setPaid(result.getBoolean("paid"));

                payments.add(payment);
            }
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }

        return payments;
    }

    public void saveAlert(Integer userId, Integer paymentId) {
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("INSERT INTO ALERT (user_id, payment_id) values (?, ?)");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, paymentId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }

    }
}
