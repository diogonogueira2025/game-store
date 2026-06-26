package com.diogonogueira.gamestore.services.exceptions;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String msg) {
        super(msg);
    }
    public ResourceNotFoundException(Object id) {
        super("Resource not found. Id: " + id);
    }
}
