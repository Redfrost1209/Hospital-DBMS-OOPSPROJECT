package com.hospital.exception;

public class EntityNotFoundException extends HospitalException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " with ID '" + id + "' was not found.");
    }
}
