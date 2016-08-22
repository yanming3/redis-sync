package com.yanming.sync.exception;

import java.io.IOException;


public class RedisResponseException extends IOException {

    private static final long serialVersionUID = 1208471190036181159L;

    public RedisResponseException() {
        super();
    }

    public RedisResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisResponseException(String message) {
        super(message);
    }

    public RedisResponseException(Throwable cause) {
        super(cause);
    }

}
