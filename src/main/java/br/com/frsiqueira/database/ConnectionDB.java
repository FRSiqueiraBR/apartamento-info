package br.com.frsiqueira.database;

import br.com.frsiqueira.BuildVars;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.sql.*;

public class ConnectionDB {
    private static final String LOGTAG = "CONNECTIONDB";
    private Connection currentConnection;

    public ConnectionDB() {
        this.currentConnection = openConexion();
    }


    private Connection openConexion() {
        Connection connection = null;
        try {
            Class.forName(BuildVars.controllerDB).newInstance();
            connection = DriverManager.getConnection(BuildVars.linkDB, BuildVars.userDB, BuildVars.password);
        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            BotLogger.error(LOGTAG, e);
        }

        return connection;
    }

    private void closeConnection() {
        try {
            this.currentConnection.close();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public ResultSet runSqlQuery(String query) throws SQLException {
        final Statement statement;
        statement = this.currentConnection.createStatement();
        return statement.executeQuery(query);
    }

    public Boolean executeQuery(String query) throws SQLException {
        final Statement statement = this.currentConnection.createStatement();
        return statement.execute(query);
    }

    public PreparedStatement getPreparedStatement(String query) throws SQLException {
        return this.currentConnection.prepareStatement(query);
    }

    public void initTransaction() throws SQLException {
        this.currentConnection.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        try{
            this.currentConnection.commit();
        } catch (SQLException e) {
            if (this.currentConnection != null) {
                this.currentConnection.rollback();
            }
        } finally {
            this.currentConnection.setAutoCommit(false);
        }
    }
}
