package com.ll.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private final String url;
    private final String user;
    private final String password;

    public ConnectionManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() {
        try{
            return DriverManager.getConnection(url, user, password);
        }catch(SQLException e){
            throw new RuntimeException("Error Database connection.", e);
        }
    }
}
