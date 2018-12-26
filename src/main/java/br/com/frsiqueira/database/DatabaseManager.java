package br.com.frsiqueira.database;

import org.telegram.telegrambots.meta.logging.BotLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public int getApeInfoState(Integer userId, Long chatId) {
        int state = 0;

        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("SELECT state FROM ApeInfoState WHERE userId = ? AND chatId = ?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setLong(2, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                state = result.getInt("state");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return state;
    }

    public boolean insertApeInfoState(Integer userId, Long chatId, int state) {
        int updateRows = 0;

        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("REPLACE INTO WeatherState (userId, chatId, state) VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userId);
            preparedStatement.setLong(2, chatId);
            preparedStatement.setInt(3, state);
        } catch (SQLException e) {
            e.printStackTrace();
        }
       return updateRows > 0;
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
}
