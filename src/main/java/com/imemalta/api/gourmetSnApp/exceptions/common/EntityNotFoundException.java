package com.imemalta.api.gourmetSnApp.exceptions.common;

public class EntityNotFoundException extends RuntimeException{
    private Object id;
    private String type;

    public EntityNotFoundException(String type, Object id) {
        this.id = id;
        this.type = type;
    }

    public Object getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
