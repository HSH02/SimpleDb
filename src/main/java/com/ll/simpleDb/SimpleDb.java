package com.ll.simpleDb;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleDb {
   private final String host;
   private final String user;
   private final String pass;
   private final String dbName;

    public void run(String sql) {
    }
}

