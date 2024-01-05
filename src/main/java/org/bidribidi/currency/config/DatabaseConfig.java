package org.bidribidi.currency.config;

import org.bidribidi.currency.utils.SqlScriptUtility;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseConfig {

    private final static String URL_PREFIX = "jdbc:sqlite:";
    private final static String DATABASE = "currency.db";
    private Connection connection;

    public DatabaseConfig() {
        try {
            Class.forName("org.sqlite.JDBC");
            String path = getClass().getClassLoader().getResource(DATABASE).getPath();
            connection = DriverManager.getConnection(URL_PREFIX + path);
            System.out.println("Connection to SQLite has been established.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: unable to load driver class");
        } catch (SQLException e2) {
            System.out.println("Error: unable to connect to database.\n" + e2.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
