package br.com.frsiqueira.database;

public class CreationString {
    static final String createUserTable = "CREATE TABLE IF NOT EXISTS user(user_id INTEGER PRIMARY KEY NOT NULL, chat_id INTEGER NOT NULL, create_date DATE NOT NULL) ENGINE=INNODB;";
    static final String createApartmentTable = "CREATE TABLE IF NOT EXISTS apartment(name VARCHAR(100) NOT NULL, address VARCHAR(100) NOT NULL, release_date DATE NOT NULL) ENGINE=INNODB;";
    static final String createPaymentTable = "CREATE TABLE IF NOT EXISTS payment(id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, parcel integer, date DATE NOT NULL, type VARCHAR(20), amount DECIMAL(13, 2) NOT NULL) ENGINE=INNODB;";
    static final String createAlertTable = "CREATE TABLE IF NOT EXISTS alert(id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, user_id INTEGER, payment_id INTEGER, FOREIGN KEY (user_id) REFERENCES user(user_id), FOREIGN KEY (payment_id) REFERENCES payment(id)) ENGINE=INNODB;";

    static final String insertApartmentTable = "INSERT INTO apartment(name, address, release_date) VALUES('LIVING CLÁSSICO', 'R. Dr. Ribeiro de Almeida, 88', '2022-03-01');";
    static final String insertPaymentTable = "INSERT INTO payment(parcel, date, type, amount) VALUES(1, '2018-11-15', 'parcel', '3011.41');";
}