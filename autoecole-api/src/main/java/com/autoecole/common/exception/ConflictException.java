package com.autoecole.common.exception;

/** Conflit métier → HTTP 409 (ex. chevauchement de leçon, doublon d'email). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
