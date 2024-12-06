package com.ll.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

@Getter
@Setter
@RequiredArgsConstructor
public class SimpleDb {
    private final ConnectionManager connectionManager;
    private final DevLogger devLogger;
    private boolean devMode;
    private Connection transactionConnection;

    public SimpleDb(String host, String user, String password, String dbName, boolean devMode) {
        String url = String.format("jdbc:mysql://%s:3306/%s", host, dbName);
        this.connectionManager = new ConnectionManager(url, user, password);
        this.devMode = devMode;
        this.devLogger = new DevLogger(devMode);
        transactionConnection = connectionManager.getConnection();
    }

    public Sql genSql() {
        Sql sql = (transactionConnection != null)
                ? new Sql(transactionConnection, devLogger)
                : new Sql(connectionManager, devLogger);
        sql.setDevMode(devMode);
        return sql;
    }

    /**
     * 파라미터 없이 간단한 SQL 실행 메서드
     * @param sql 실행할 SQL 쿼리 문자열
     */
    public void run(String sql) {
        run(sql, new Object[]{});
    }

    public void run(String sql, Object... params) {
        if (devMode) {
            System.out.printf("[DEV MODE] Executing SQL: %s%n", sql);
            if (params.length > 0) {
                System.out.printf("[DEV MODE] With Parameters: %s%n", Arrays.toString(params));
            }
        }

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql) 
        ) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            System.out.println("[DEBUG] Executing statement: " + preparedStatement);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    @SneakyThrows
    public void startTransaction() {
        if(transactionConnection != null) {
            transactionConnection = connectionManager.getConnection();
            transactionConnection.setAutoCommit(false);
            System.out.println("트랜잭션 시작");
        }
    }

    @SneakyThrows
    public void rollback() {
        if(transactionConnection != null) {
            transactionConnection.rollback();
            transactionConnection.close();
            transactionConnection = null;
            System.out.println("롤백");
        }
    }

    @SneakyThrows
    public void commit() {
        if(transactionConnection != null) {
            transactionConnection.commit();
            transactionConnection.close();
            transactionConnection = null;
            System.out.println("커밋");
        }
    }
}