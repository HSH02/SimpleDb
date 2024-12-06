package com.ll.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * 프로그램 DB 시작 설정 관리
 */
@Getter
@Setter
@RequiredArgsConstructor
public class SimpleDb {
    // ConnectionManager 객체: 데이터베이스 연결을 관리하는 클래스
    private final ConnectionManager connectionManager;
    // DevLogger 객체: 개발자 모드에서 실행 정보를 출력하는 로깅 클래스
    private final DevLogger devLogger;
    // 개발자 모드 플래그: true이면 개발자 모드에서 실행
    private boolean devMode;

    /**
     * SimpleDb 클래스 생성자
     * @param host 데이터베이스 호스트 주소
     * @param user 데이터베이스 사용자 이름
     * @param password 데이터베이스 비밀번호
     * @param dbName 데이터베이스 이름
     * @param devMode 개발자 모드 활성화 여부
     */
    public SimpleDb(String host, String user, String password, String dbName, boolean devMode) {
        // URL을 형식에 맞춰서 생성 (호스트와 데이터베이스 이름 포함)
        String url = String.format("jdbc:mysql://%s:3306/%s", host, dbName);
        this.connectionManager = new ConnectionManager(url, user, password);
        this.devMode = devMode;
        this.devLogger = new DevLogger(devMode);
    }

    /**
     * Sql 객체 생성 및 개발자 모드 설정
     * @return 생성된 Sql 객체
     */
    public Sql genSql() {
        // Sql 객체 생성 후 개발자 모드 설정
        Sql sql = new Sql(connectionManager, devLogger);
        sql.setDevMode(devMode);
        return sql;
    }

    /**
     * 파라미터 없이 간단한 SQL 실행 메서드
     * @param sql 실행할 SQL 쿼리 문자열
     */
    public void run(String sql) {
        // 파라미터 없는 경우 빈 배열로 호출
        run(sql, new Object[]{});
    }

    /**
     * 파라미터를 포함한 SQL 실행 메서드
     * @param sql 실행할 SQL 쿼리 문자열
     * @param params SQL 쿼리에 바인딩할 파라미터들
     */
    public void run(String sql, Object... params) {
        // 개발자 모드가 활성화된 경우 실행할 SQL과 파라미터를 출력
        if (devMode) {
            System.out.printf("[DEV MODE] Executing SQL: %s%n", sql);
            if (params.length > 0) {
                System.out.printf("[DEV MODE] With Parameters: %s%n", Arrays.toString(params));
            }
        }

        // try-with-resources를 사용하여 Connection과 PreparedStatement 자동으로 닫음
        try (
                Connection connection = connectionManager.getConnection(); // 데이터베이스 연결 객체 생성
                PreparedStatement preparedStatement = connection.prepareStatement(sql) // SQL 실행을 위한 PreparedStatement 생성
        ) {
            // 파라미터를 PreparedStatement에 설정
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            // 디버그 모드에서 실행할 쿼리 출력
            System.out.println("[DEBUG] Executing statement: " + preparedStatement);
            // SQL 실행
            preparedStatement.execute();
        } catch (SQLException e) {
            // SQL 실행 중 오류 발생 시 런타임 예외로 래핑하여 던짐
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }
}