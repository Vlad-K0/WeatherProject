package com.vladko.Exceptions;

import java.sql.SQLException;

public class NotFoundPassword extends RuntimeException {
    public NotFoundPassword(String message) {
        super(message);
    }
}
