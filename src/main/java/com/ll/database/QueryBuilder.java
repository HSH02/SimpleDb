package com.ll.database;

public class QueryBuilder {
    private final StringBuilder queryBuilder;

    public QueryBuilder() {
        this.queryBuilder = new StringBuilder();
    }

    public QueryBuilder append(String query) {
        queryBuilder.append(query).append(" ");
        return this; // 메서드 체이닝을 위해 자신을 반환
    }

    public String build() {
        return queryBuilder.toString().trim();
    }

    @Override
    public String toString() {
        return queryBuilder.toString();
    }
}
