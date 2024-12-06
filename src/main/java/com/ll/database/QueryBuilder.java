package com.ll.database;

/**
 * SQL 쿼리 문자열을 구축
 */
class QueryBuilder {
    private final StringBuilder queryBuilder = new StringBuilder();

    public QueryBuilder append(String query) {
        queryBuilder.append(query).append(" ");
        System.out.println("[DEBUG] QueryBuilder appending: " + query);
        return this;
    }

    public String build() {
        String builtQuery = queryBuilder.toString().trim();
        System.out.println("[DEBUG] QueryBuilder built query: " + builtQuery);
        return builtQuery;
    }
}

