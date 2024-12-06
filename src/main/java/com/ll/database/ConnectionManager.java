package com.ll.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB 연결 관리
 */
@Getter
@RequiredArgsConstructor
public class ConnectionManager {
    private final String url;
    private final String user;
    private final String password;

    public Connection getConnection() {
        try {
            System.out.println("[DEBUG] Establishing connection to database: " + url);
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("Error while connecting to database", e);
        }
    }
}
