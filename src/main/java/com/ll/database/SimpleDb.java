package com.ll.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SimpleDb {
    private final ConnectionManager connectionManager;

    public SimpleDb(String host, String user, String password, String dbName) {
        String url = String.format("jdbc:mysql://%s:3306/%s", host, dbName); // 포트 번호 3306
        this.connectionManager = new ConnectionManager(url, user, password);
    }

    public Sql genSql() {
        return new Sql(connectionManager); // ConnectionManager를 전달하여 Sql 객체 생성
    }

    public void run(String sql) {
        try (
                Connection connection = connectionManager.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    public void run(String sql, Object... params) {
        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL with parameters: " + sql, e);
        }
    }
}
