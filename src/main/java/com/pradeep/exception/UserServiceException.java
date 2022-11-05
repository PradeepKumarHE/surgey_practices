package com.pradeep.exception;

import java.io.Serial;

public class UserServiceException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = -4061543441269434391L;

    public UserServiceException(String message) {
        super(message);
    }
}
