package br.com.frsiqueira.database;

import org.telegram.telegrambots.meta.logging.BotLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

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
}
