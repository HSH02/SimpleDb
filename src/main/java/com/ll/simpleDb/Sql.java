package com.ll.simpleDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sql {
    private final List<Object> params;
    private final StringBuilder queryBuilder;
    private final SimpleDb simpleDb;

    // 생성자 : 객체를 받아 초기화한다.
    public Sql(SimpleDb simpleDb) {
        this.queryBuilder = new StringBuilder();
        this.params = new ArrayList<Object>();
        this.simpleDb = simpleDb;
    }


    public Sql append(String query){
        queryBuilder.append(query).append(" ");
        return this;  // 메서드 체이닝을 위한 자신 반환
    }

    public Sql append(String query, Object... parameter){
        queryBuilder.append(query).append(" ");

        for(Object param : parameter){
            params.add(param);
        }

        return this;    // 메서드 체이닝을 위한 자신 반환
    }

    public List<Map<String, Object>> selectRows(){
        String sql = queryBuilder.toString();

        try(
                Connection connection = DriverManager.getConnection(simpleDb.getUrl(), simpleDb.getUser(), simpleDb.getPassword());
                PreparedStatement  preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery() // 쿼리 실행 후 결과를 반환
        ) {
            // 결과를 저장할 리스트 생성
            List<Map<String, Object>> rows = new ArrayList<>();

            // ResultSet의 메타데이터를 통해 컬럼 정보를 가져오기
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 각 행을 반복하여 데이터를 Map으로 변환하여 리스트에 추가
            while(resultSet.next()){
                Map<String, Object> row = new HashMap<>();

                for(int i = 1; i <= columnCount; i++){
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    row.put(columnName, columnValue);
                }
            }

            return rows; //모든 행을 포함한 리스트 반환
        } catch (SQLException e){
            throw new RuntimeException("Error excuting SQL : " + sql, e);
        }
    }

    public long insert(){
        String sql = queryBuilder.toString();

        try(
                Connection connection  = DriverManager.getConnection(simpleDb.getUrl(), simpleDb.getUser(), simpleDb.getPassword());
                PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
                // 파라미터 설정
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(1 + i, params.get(i));
                }

                // SQL 실행
                preparedStatement.execute();

                try(ResultSet generatedKey = preparedStatement.getGeneratedKeys()) {
                    if(generatedKey.next()){
                        return generatedKey.getLong(1); // 생성된 ID 반환
                    } else {
                        throw new SQLException();
                    }
                }

        } catch (SQLException e){
            throw new RuntimeException("Error excuting SQL : " + sql, e);
        }
    }

    public int update() {
        String sql = queryBuilder.toString();

        try (
                Connection connection = DriverManager.getConnection(simpleDb.getUrl(), simpleDb.getUser(), simpleDb.getPassword());
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            // 파라미터 설정
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i)); // 파라미터 인덱스는 1부터 시작
            }

            // 쿼리 실행 및 영향받은 행의 개수 반환
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows;

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    public int delete() {
        String sql = queryBuilder.toString();

        try (
                Connection connection = DriverManager.getConnection(simpleDb.getUrl(), simpleDb.getUser(), simpleDb.getPassword());
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            // 파라미터 설정
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i)); // 파라미터 인덱스는 1부터 시작
            }

            // 쿼리 실행 및 영향받은 행의 개수 반환
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows;

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }



}
