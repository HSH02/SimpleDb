package com.ll.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

/**
 * 개발자 모드에서 쿼리 실행 정보를 출력하는 로그 관리
 */
@Getter
@Setter
public class DevLogger {
    private boolean devMode;

    public DevLogger(boolean devMode) {
        this.devMode = devMode;
    }

    public void logQuery(String query, Object... params) {
        if (devMode) {
            System.out.println("[DEV MODE] Query: " + query);
            System.out.println("[DEV MODE] Parameters: " + Arrays.toString(params));
        }
    }

    public void logResult(Object result) {
        if (devMode) {
            System.out.println("[DEV MODE] Result: " + result);
        }
    }
}