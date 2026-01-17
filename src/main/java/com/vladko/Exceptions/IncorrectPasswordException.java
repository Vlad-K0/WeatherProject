package com.vladko.Exceptions;


import java.sql.SQLException;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException(String s, SQLException e) {
        super(s, e);
    }
}
