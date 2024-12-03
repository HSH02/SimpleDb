package com.ll.simpleDb;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class SimpleDbTest {
    private static SimpleDb simpleDb;

    @BeforeAll
    public static void beforeAll() {
        simpleDb = new SimpleDb("localhost", "root", "lldj123414", "simpleDb__test");

        createArticleTable();
    }

    private static void createArticleTable() {
        simpleDb.run("DROP TABLE IF EXISTS article");

        simpleDb.run("""
                CREATE TABLE article (
                    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    PRIMARY KEY(id),
                    createdDate DATETIME NOT NULL,
                    modifiedDate DATETIME NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    `body` TEXT NOT NULL,
                    isBlind BIT(1) NOT NULL DEFAULT 0
                )
                """);
    }

    @Test
    @DisplayName("데이터베이스 로드")
    public void t000(){

    }

}