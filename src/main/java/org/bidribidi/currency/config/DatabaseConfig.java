package org.bidribidi.currency.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseConfig {
    private final static String URL_PREFIX = "jdbc:sqlite:";
    private final static String DATABASE = "currency.db";
    private final static HikariConfig config = new HikariConfig();
    private final static HikariDataSource dataSource;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String path = Objects.requireNonNull(DatabaseConfig.class.getClassLoader().getResource(DATABASE)).getPath();
        config.setJdbcUrl(URL_PREFIX + path);
        config.setMaximumPoolSize(20);
        dataSource = new HikariDataSource(config);
    }

    public DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
