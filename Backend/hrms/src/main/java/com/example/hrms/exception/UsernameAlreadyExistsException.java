package com.example.hrms.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String msg) { super(msg); }
}
