package com.imemalta.api.gourmetSnApp.dtos;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.HashMap;
import java.util.Map;


public class ApiError {

    @Enumerated(EnumType.STRING)
    private ApiErrorCode code;
    private String description;
    private Map<String,Object> metaData = new HashMap<>();

    public ApiError(ApiErrorCode code, String description) {
        this.code = code;
        this.description = description;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public void setCode(ApiErrorCode code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void addToMetaData(String key, Object value) {
        metaData.put(key, value);
    }

}
