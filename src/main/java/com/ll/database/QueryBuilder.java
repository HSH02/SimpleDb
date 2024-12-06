package com.ll.database;

/**
 * SQL 쿼리 문자열을 구축
 */
class QueryBuilder {
    private final StringBuilder queryBuilder = new StringBuilder();

    public QueryBuilder append(String query) {
        queryBuilder.append(query).append(" ");
        return this;
    }

    public String build() {
        return queryBuilder.toString().trim();
    }
}