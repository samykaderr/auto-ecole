package com.autoecole.common.exception;

/** Ressource introuvable ou hors périmètre du tenant → HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String entity, Object id) {
        super(entity + " " + id + " introuvable");
    }
}
