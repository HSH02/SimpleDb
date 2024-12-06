package com.ll.database;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Sql {
    private final List<Object> params = new ArrayList<>();
    private final QueryBuilder queryBuilder;
    private final ConnectionManager connectionManager;
    private final DevLogger devLogger;

    // 생성자 : 객체를 받아 초기화한다.
    public Sql(ConnectionManager connectionManager, DevLogger devLogger) {
        this.queryBuilder = new QueryBuilder();
        this.connectionManager = connectionManager;
        this.devLogger = devLogger;
    }

    // 나머지 메서드들 유지
    public Sql setDevMode(boolean devMode) {
        devLogger.setDevMode(devMode);
        return this; // 메서드 체이닝
    }

    /**
     * append 메서드: 쿼리를 추가하고 파라미터를 설정합니다.
     * @param query 추가할 SQL 쿼리
     * @param parameters 쿼리에 사용할 파라미터들
     * @return 현재 Sql 객체 (메서드 체이닝을 위해)
     */
    public Sql append(String query, Object... parameters) {
        return appendQueryWithValidation(query, parameters);
    }

    /**
     * appendIn 메서드: IN 절을 포함한 쿼리를 추가하고 파라미터를 설정합니다.
     * @param baseQuery IN 절을 포함한 기본 쿼리 (물음표를 포함해야 함)
     * @param parameters IN 절에 사용할 파라미터들
     * @return 현재 Sql 객체 (메서드 체이닝을 위해)
     */
    public Sql appendIn(String baseQuery, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            throw new IllegalArgumentException("IN clause requires at least one parameter.");
        }
        // IN 절에 사용할 ? 플레이스홀더를 생성하여 쿼리에 삽입
        String placeholders = String.join(", ", Collections.nCopies(parameters.length, "?"));
        String modifiedQuery = baseQuery.replace("?", placeholders);
        return appendQueryWithValidation(modifiedQuery, parameters);
    }

    /**
     * selectRows 메서드: SQL SELECT 문을 실행하여 결과를 List<Map<String, Object>> 타입으로 반환합니다.
     * @return 각 행의 데이터를 컬럼명과 매핑한 리스트
     */
    public List<Map<String, Object>> selectRows() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParams(preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return extractRows(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    /**
     * selectRows 메서드: SQL SELECT 문을 실행하여 결과를 주어진 클래스 타입으로 매핑하여 반환합니다.
     * @param clazz 매핑할 클래스 타입
     * @param <T> 클래스 타입
     * @return 각 행을 주어진 클래스 타입으로 매핑한 리스트
     */
    public <T> List<T> selectRows(Class<T> clazz) {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParams(preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return mapResultSetToObjects(resultSet, clazz);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    /**
     * selectRow 메서드: SQL SELECT 문을 실행하여 첫 번째 행의 데이터를 반환합니다.
     * @return 첫 번째 행의 데이터를 컬럼명과 매핑한 Map 객체 (없을 경우 null)
     */
    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * selectLong 메서드: SQL SELECT 문을 실행하여 결과를 Long 타입으로 반환합니다.
     * @return 첫 번째 컬럼의 Long 값 (없을 경우 null)
     */
    public Long selectLong() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                // 데이터베이스 연결 및 SQL 실행 준비
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            // PreparedStatement에 파라미터 설정
            setParams(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e); // SQL 실행 중 오류 발생 시 런타임 예외 던지기
        }
    }

    /**
     * selectLongs 메서드: SQL SELECT 문을 실행하여 결과를 List<Long> 타입으로 반환합니다.
     * @return 첫 번째 컬럼의 Long 값들을 포함한 리스트 (없을 경우 빈 리스트 반환)
     */
    public List<Long> selectLongs() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            setParams(preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Long> values = new ArrayList<>();
                while (resultSet.next()) {
                    values.add(resultSet.getLong(1));
                }
                return values;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    /**
     * selectString 메서드: SQL SELECT 문을 실행하여 결과를 String 타입으로 반환합니다.
     * @return 첫 번째 컬럼의 String 값 (없을 경우 null)
     */
    public String selectString() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParams(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    /**
     * selectBoolean 메서드: SQL SELECT 문을 실행하여 결과를 Boolean 타입으로 반환합니다.
     * @return 첫 번째 컬럼의 Boolean 값 (없을 경우 null)
     */
    public Boolean selectBoolean() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParams(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Object columnValue = resultSet.getObject(1);
                    if (columnValue instanceof Boolean) {
                        return (Boolean) columnValue;
                    } else if (columnValue instanceof Number) {
                        return ((Number) columnValue).intValue() != 0;
                    }
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }



    /**
     * selectDatetime 메서드: SQL SELECT 문을 실행하여 결과를 LocalDateTime 타입으로 반환합니다.
     * @return 첫 번째 컬럼의 LocalDateTime 값 (없을 경우 null)
     */
    public LocalDateTime selectDatetime() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParams(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp(1);
                    return timestamp != null ? timestamp.toLocalDateTime() : null;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e);
        }
    }

    /**
     * insert 메서드: SQL INSERT 문을 실행하고 생성된 키(ID)를 반환합니다.
     * @return 생성된 키 값 (예: 자동 증가된 ID)
     */
    public long insert() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        try (
                // 데이터베이스 연결을 얻고 자동 생성된 키를 반환하도록 준비된 SQL 실행 준비
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            // PreparedStatement 파라미터 설정
            setParams(preparedStatement);
            preparedStatement.executeUpdate();

            // 생성된 키를 얻기 위한 ResultSet 사용
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1); // 생성된 키 반환
                } else {
                    throw new SQLException("No ID obtained."); // 생성된 키가 없을 경우 예외 발생
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e); // SQL 실행 중 오류 발생 시 런타임 예외 던지기
        }
    }

    /**
     * update 메서드: SQL UPDATE 문을 실행하고 영향을 받은 행 수를 반환합니다.
     * @return 업데이트된 행 수
     */
    public int update() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        return executeUpdate(sql); // SQL 실행 결과로 영향을 받은 행 수 반환
    }

    /**
     * delete 메서드: SQL DELETE 문을 실행하고 영향을 받은 행 수를 반환합니다.
     * @return 삭제된 행 수
     */
    public int delete() {
        String sql = queryBuilder.build();
        devLogger.logQuery(sql, params.toArray());

        return executeUpdate(sql); // SQL 실행 결과로 영향을 받은 행 수 반환
    }

    /**
     * executeUpdate 메서드: SQL UPDATE 또는 DELETE 문을 실행합니다.
     * @param sql 실행할 SQL 쿼리
     * @return 영향을 받은 행 수
     */
    private int executeUpdate(String sql) {
        try (
                // 데이터베이스 연결 및 SQL 실행 준비
                Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            // PreparedStatement 파라미터 설정
            setParams(preparedStatement);
            return preparedStatement.executeUpdate(); // 쿼리 실행 후 영향을 받은 행 수 반환
        } catch (SQLException e) {
            throw new RuntimeException("Error executing SQL: " + sql, e); // SQL 실행 중 오류 발생 시 런타임 예외 던지기
        }
    }

    /**
     * extractRows 메서드: ResultSet에서 모든 행을 추출하여 List<Map<String, Object>> 형태로 반환합니다.
     * @param resultSet SQL 실행 결과
     * @return 각 행의 데이터를 컬럼명과 매핑한 리스트
     * @throws SQLException 결과 추출 중 오류 발생 시 예외 던지기
     */
    private List<Map<String, Object>> extractRows(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    private <T> List<T> mapResultSetToObjects(ResultSet resultSet, Class<T> clazz) throws SQLException {
        List<T> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()){
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i); // 컬럼명을 소문자로 변환
                    Object columnValue = resultSet.getObject(i);

                    Field field;
                    try {
                        field = clazz.getDeclaredField(columnName);
                    } catch (NoSuchFieldException e) {
                        continue;
                    }

                    field.setAccessible(true);
                    if (field.getType() == int.class && columnValue instanceof Number) {
                        field.set(instance, ((Number) columnValue).intValue());
                    } else if (field.getType() == long.class && columnValue instanceof Number) {
                        field.set(instance, ((Number) columnValue).longValue());
                    } else if (field.getType() == boolean.class && columnValue instanceof Boolean) {
                        field.set(instance, columnValue);
                    } else if (field.getType() == LocalDateTime.class && columnValue instanceof Timestamp) {
                        field.set(instance, ((Timestamp) columnValue).toLocalDateTime());
                    } else {
                        field.set(instance, columnValue);
                    }
                }
                resultList.add(instance);
            } catch (ReflectiveOperationException e){
                throw new RuntimeException("Error mapping result set to object: " + clazz.getName(), e);
            }
        }

        return resultList;
    }

    /**
     * setParams 메서드: PreparedStatement 파라미터를 설정합니다.
     * @param preparedStatement SQL 실행 준비 객체
     * @throws SQLException 파라미터 설정 중 오류 발생 시 예외 던지기
     */
    private void setParams(PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            preparedStatement.setObject(i + 1, params.get(i)); // SQL 1부터 시작하는 인덱스로 파라미터 설정
        }
    }

    /**
     * appendQueryWithValidation 메서드: 쿼리를 추가하고 파라미터를 설정하며, 개발자 모드가 활성화된 경우 쿼리를 로깅합니다.
     * @param query 추가할 SQL 쿼리
     * @param parameters 쿼리에 사용할 파라미터들
     * @return 현재 Sql 객체 (메서드 체이닝을 위해)
     */
    private Sql appendQueryWithValidation(String query, Object... parameters) {
        devLogger.logQuery(query, parameters); // 쿼리와 파라미터를 로깅
        queryBuilder.append(query); // 쿼리를 빌더에 추가
        params.addAll(Arrays.asList(parameters)); // 파라미터를 리스트에 추가
        return this;
    }
}
