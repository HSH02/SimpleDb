package com.ll.simpleDb;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SimpleDb 클래스는 MySQL 데이터베이스와의 연결 및 기본적인 SQL 실행을 지원합니다.
 * 사용자는 이 클래스를 통해 간단한 SQL 쿼리 실행 및 파라미터 처리된 쿼리를 수행할 수 있습니다.
 */
public class SimpleDb {

    @Getter
    private final String url; // MySQL 데이터베이스 URL
    @Getter
    private final String user; // 데이터베이스 사용자명
    @Getter
    private final String password; // 데이터베이스 비밀번호

    /**
     * SimpleDb 생성자
     * 데이터베이스 연결을 초기화하며, MySQL 드라이버를 로드합니다.
     *
     * @param host 데이터베이스 호스트 주소
     * @param user 데이터베이스 사용자명
     * @param password 데이터베이스 비밀번호
     * @param dbName 데이터베이스 이름
     */
    public SimpleDb(String host, String user, String password, String dbName) {
        this.url = String.format("jdbc:mysql://%s/%s", host, dbName);
        this.user = user;
        this.password = password;

        try {
            // MySQL JDBC 드라이버를 메모리에 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    /**
     * SQL 쿼리를 실행합니다 (파라미터가 없는 경우 사용).
     *
     * @param sql 실행할 SQL 쿼리 문자열
     */
    public void run(String sql) {
        try (
                Connection connection = DriverManager.getConnection(url, user, password); // 데이터베이스 연결 생성
                Statement statement = connection.createStatement() // 단순 쿼리를 실행하기 위한 Statement 생성
        ) {
            statement.execute(sql); // SQL 쿼리 실행
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e); // 예외 발생 시 메시지와 함께 던짐
        }
    }

    /**
     * 파라미터를 포함한 SQL 쿼리를 실행합니다.
     *
     * @param sql 실행할 SQL 쿼리 문자열 (PreparedStatement 사용)
     * @param params SQL에 전달할 파라미터들
     */
    public void run(String sql, Object... params) {
        try (
                Connection connection = DriverManager.getConnection(url, user, password); // 데이터베이스 연결 생성
                PreparedStatement preparedStatement = connection.prepareStatement(sql) // 파라미터를 처리하기 위한 PreparedStatement 생성
        ) {
            // 파라미터 바인딩
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            preparedStatement.execute(); // SQL 쿼리 실행

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL with parameters: " + sql, e); // 예외 발생 시 메시지와 함께 던짐
        }
    }

    public Sql genSql() {
        return new Sql(this); // SimpleDb 객체를 전달하여 Sql 객체 생성
    }
}
