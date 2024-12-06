package com.ll.database;

import com.ll.standard.util.Ut;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Sql {
    private final List<Object> params = new ArrayList<>();
    private final QueryBuilder queryBuilder;
    private final ConnectionManager connectionManager;
    private final Connection connection;
    private final DevLogger devLogger;

    // 생성자 : 객체를 받아 초기화한다.
    public Sql(ConnectionManager connectionManager, DevLogger devLogger) {
        this.queryBuilder = new QueryBuilder();
        this.connectionManager = connectionManager;
        this.devLogger = devLogger;
        connection = connectionManager.getConnection();
    }

    public Sql(Connection connection, DevLogger devLogger) {
        this.queryBuilder = new QueryBuilder();
        this.connectionManager = null;
        this.devLogger = devLogger;
        this.connection = connection;
    }

    // 나머지 메서드들 유지
    public void setDevMode(boolean devMode) {
        devLogger.setDevMode(devMode);
    }

    public Sql append(String query, Object... parameters) {
        queryBuilder.append(query); // 쿼리를 빌더에 추가
        params.addAll(Arrays.asList(parameters)); // 파라미터를 리스트에 추가
        return this;
    }

    public Sql appendIn(String baseQuery, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            throw new IllegalArgumentException("IN clause requires at least one parameter.");
        }
        // IN 절에 사용할 ? 플레이스홀더를 생성하여 쿼리에 삽입
        String placeholders = String.join(", ", Collections.nCopies(parameters.length, "?"));
        String modifiedQuery = baseQuery.replace("?", placeholders);
        return append(modifiedQuery, parameters);
    }


    private <T> T run(Class<T> tclass) {
        String sql = queryBuilder.build();

        devLogger.logQuery(sql, params.toArray());

        try(
                PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
                ){

            setParams(preparedStatement);

            if(sql.startsWith("INSERT")) {
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                return (generatedKeys.next() && tclass == Long.class) ? (T) (Long) generatedKeys.getLong(1) : (T) (Integer) preparedStatement.getUpdateCount();
            }

            if(sql.startsWith("SELECT")) {
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    return parseResultSet(resultSet, tclass);
                }
            }

            return (T)(Integer) preparedStatement.executeUpdate();
        } catch (SQLException e ) {
            throw new RuntimeException("SQL Execution failed: " + e.getMessage(), e);
        }
    }

    private <T> T parseResultSet(ResultSet resultSet, Class<T> cls) throws SQLException {
        if (!resultSet.next()) throw new NoSuchElementException("No data found");

        return switch (cls.getSimpleName()) {
            case "String" -> (T) resultSet.getString(1);
            case "List" -> {
                List<Map<String, Object>> rows = new ArrayList<>();
                do {
                    rows.add(parseResultSetToMap(resultSet));
                } while (resultSet.next());
                yield (T) rows;
            }
            case "Map" -> (T) parseResultSetToMap(resultSet);
            case "LocalDateTime" -> (T) resultSet.getTimestamp(1).toLocalDateTime();
            case "Long" -> (T) (Long) resultSet.getLong(1);
            case "Boolean" -> (T) (Boolean) resultSet.getBoolean(1);
            default -> throw new IllegalArgumentException("Unsupported class type: " + cls.getSimpleName());
        };
    }

    // ResultSet을 Map으로 변환
    private Map<String, Object> parseResultSetToMap(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = switch (metaData.getColumnType(i)) {
                case Types.BIGINT -> resultSet.getLong(columnName);
                case Types.TIMESTAMP -> {
                    Timestamp timestamp = resultSet.getTimestamp(columnName);
                    yield (timestamp != null) ? timestamp.toLocalDateTime() : null;
                }
                case Types.BOOLEAN -> resultSet.getBoolean(columnName);
                default -> resultSet.getObject(columnName);
            };
            row.put(columnName, value);
        }
        return row;
    }

    public long insert() {
        return run(Long.class);
    }

    public int update() {
        return run(Integer.class);
    }

    public int delete() {
        return run(Integer.class);
    }

    public Map<String, Object> selectRow(){
        return run(Map.class);
    }

    public List<Map<String, Object>> selectRows() {
        return run(List.class);
    }

    public <T> T selectRow(Class<T> tClass){
        return Ut.mapper.mapToObj(selectRow(), tClass);
    }

    public <T> List<T> selectRows(Class<T> tclass) {
        List<Map<String, Object>> rows = selectRows();

        return rows.stream()
                .map(row -> Ut.mapper.mapToObj(row, tclass))  // 각각의 행을 T 타입 객체로 변환
                .collect(Collectors.toList());  // 결과를 List<T> 형태로 수집하여 반환
    }

    public LocalDateTime selectDatetime() {
        return run(LocalDateTime.class);
    }

    public Long selectLong(){
        return run(Long.class);
    }

    public List<Long> selectLongs() {
        return selectRows()
                .stream()    // selectRows() 결과를 스트림으로 변환
                .map(row -> (Long) row.values().iterator().next()) //각 row의 첫 번째 값(Long으로 캐스팅)을 매핑
                .toList();    // 스트림의 결과를 List<Long> 형태로 반환
    }

    public String selectString(){
        return run(String.class);
    }

    public Boolean selectBoolean(){
        return run(Boolean.class);
    }

    private void setParams(PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            preparedStatement.setObject(i + 1, params.get(i)); // SQL 1부터 시작하는 인덱스로 파라미터 설정
        }
    }

}