package com.ll.simpleDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        // 파라미터가 있는 위치를 동적으로 생성
        List<Object> inParams = params.subList(1, params.size()); // 첫 번째 파라미터는 title, 그 이후가 IN 절에 사용될 것들
        String inPlaceholders = String.join(", ", inParams.stream().map(param -> "?").toArray(String[]::new));

        // 동적으로 IN 절의 ?를 구성한 후 쿼리 생성
        String sql = String.format("UPDATE article SET title = ? WHERE id IN (%s)", inPlaceholders);

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
