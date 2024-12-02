package com.ll.simpleDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDb {
    // 데이터베이스 연결 정보
    private Connection connection;
    private final String host;
    private final String username;
    private final String password;
    private final String database;
    private boolean devMode = false;


    public SimpleDb(String host, String username, String password, String database){
        this.host = "jdbc:mysql://" + host + ":3306/";
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public void setDevMode(boolean devMode){
        this.devMode = devMode;

        if(devMode) {
            System.out.println("Dev mode enabled");
        } else {
            System.out.println("Dev mode disabled");
        }
    }


    public void connect() throws SQLException {
        try {
            if(connection == null || connection.isClosed()){
                connection = DriverManager.getConnection(host, username, password);
                System.out.println("DB Connection Successful.");
            } else{
                System.out.println("DB Connection Already Exists.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    public void disconnect() throws SQLException {
        try {
            if(connection != null || !connection.isClosed()){
                connection.close();
                System.out.println("DB Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to disconnect from database: " + e.getMessage());
        }
    }


    public boolean isConnected(){
        try {
            return connection != null && !connection.isClosed();
        } catch(SQLException e){
            System.err.println("Failed to check if database is connected: " + e.getMessage());
            return false;
        }
    }


    public void run(String sql) {
    }
}

