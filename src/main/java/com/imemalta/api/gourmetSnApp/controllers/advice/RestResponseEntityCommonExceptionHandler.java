package com.imemalta.api.gourmetSnApp.controllers.advice;

import com.imemalta.api.gourmetSnApp.dtos.ApiError;
import com.imemalta.api.gourmetSnApp.dtos.ApiErrorCode;
import com.imemalta.api.gourmetSnApp.exceptions.common.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseBody
public class RestResponseEntityCommonExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    protected ApiError handleConstraintViolation(ConstraintViolationException ex) {
        return ex.getApiError();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    protected ApiError handleEntityNotFound(EntityNotFoundException ex) {
        ApiError apiError = new ApiError(ApiErrorCode.NOT_FOUND, "Entity type '" + ex.getType() + "' with id '" + ex.getId() + "' was not found");

        apiError.addToMetaData("type", ex.getType());
        apiError.addToMetaData("id", ex.getId());

        return apiError;
    }

    @ExceptionHandler(NoSubscriptionException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleNoSubscription() {
        return new ApiError(ApiErrorCode.NO_SUBSCRIPTION, "User has no subscription in place");
    }

    @ExceptionHandler(UnpaidSubscriptionException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleUnpaidSubscription() {
        return new ApiError(ApiErrorCode.UNPAID_SUBSCRIPTION, "User has pending bills");
    }

    @ExceptionHandler(StripeRuntimeException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    protected ApiError handleStripeRuntime(StripeRuntimeException ex) {
        return new ApiError(ApiErrorCode.STRIPE_ERROR, ex.getStripeException().getMessage());
    }
}
