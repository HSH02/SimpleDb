package com.ll.simpleDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SimpleDb {

    private final String url;
    private final String user;
    private final String password;

    public SimpleDb(String host, String user, String password, String dbName) {
        this.url = String.format("jdbc:mysql://%s/%s", host, dbName);
        this.user = user;
        this.password = password;

        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    /**
     * Executes a SQL query (e.g., CREATE, DROP, INSERT, UPDATE).
     *
     * @param sql the SQL statement to execute
     */
    public void run(String sql) {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

             statement.execute(sql);

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    /**
     * Executes a SQL query with parameters.
     *
     * @param sql the SQL statement to execute
     * @param params the parameters to bind
     */
    public void run(String sql, Object... params) {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL with parameters: " + sql, e);
        }
    }
}
