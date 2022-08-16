package com.imemalta.api.gourmetSnApp.exceptions.common;

import com.imemalta.api.gourmetSnApp.dtos.ApiError;

public class ConstraintViolationException extends RuntimeException{
    private ApiError apiError;

    public ConstraintViolationException(ApiError apiError) {
        super();
        this.apiError = apiError;
    }

    public ApiError getApiError() {
        return apiError;
    }
}
