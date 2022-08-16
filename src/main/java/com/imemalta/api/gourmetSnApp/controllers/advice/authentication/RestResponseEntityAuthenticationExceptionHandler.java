package com.imemalta.api.gourmetSnApp.controllers.advice.authentication;

import com.imemalta.api.gourmetSnApp.dtos.ApiError;
import com.imemalta.api.gourmetSnApp.dtos.ApiErrorCode;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseBody
public class RestResponseEntityAuthenticationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    protected ApiError handleUserNotFound() {
        return new ApiError(ApiErrorCode.USER_NOT_FOUND, "Username not registered.");
    }

    @ExceptionHandler(ChangeOwnUserRolesException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleModifyingOfSelfAccount() {
        return new ApiError(ApiErrorCode.CHANGE_OWN_ROLES, "Cannot change you own user roles.");
    }

    @ExceptionHandler(NoUserInSessionException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleNoUserIsSession() {
        return new ApiError(ApiErrorCode.NOT_LOGGED_IN, "You need to log in to access first.");
    }

    @ExceptionHandler(InvalidTokenTypeException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    protected ApiError handleInvalidTokenType() {
        return new ApiError(ApiErrorCode.INVALID_TOKEN_TYPE, "Cannot use this token for the requested action.");
    }

    @ExceptionHandler(TokenNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    protected ApiError handleTokenNotFound() {
        return new ApiError(ApiErrorCode.TOKEN_NOT_FOUND, "Token was not found.");
    }

    @ExceptionHandler(ExpiredTokenException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    protected ApiError handleExpiredToken() {
        return new ApiError(ApiErrorCode.EXPIRED_TOKEN,"Token has expired.");
    }

    @ExceptionHandler(InvalidAccountStateException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    protected ApiError handleAccountAlreadyActive() {
        return new ApiError(ApiErrorCode.ACCOUNT_ALREADY_ACTIVE, "The current account state does not allow this request");
    }

    @ExceptionHandler(InvalidAccountStateChangeException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    protected ApiError handleInvalidAccountStateChangeActive() {
        return new ApiError(ApiErrorCode.INVALID_ACCOUNT_STATE_CHANGE, "Cannot change account state to PENDING_PASSWORD/PENDING_ACTIVATION");
    }

    @ExceptionHandler(PasswordNotSecureException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    protected ApiError handlePasswordNotSecure() {
        return new ApiError(ApiErrorCode.PASSWORD_NOT_SECURE,"Password is not secure.");
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleIncorrectPassword() {
        return new ApiError(ApiErrorCode.INVALID_PASSWORD, "Incorrect Password.");
    }

    @ExceptionHandler(UsernameAlreadyInUseException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    protected ApiError handleUsernameAlreadyInUse() {
        return new ApiError(ApiErrorCode.DUPLICATE_USERNAME, "Username is already registered.");
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    protected ApiError handleEmailAlreadyRegistered() {
        return new ApiError(ApiErrorCode.DUPLICATE_EMAIL, "Email is already registered.");
    }

    @ExceptionHandler(DeleteOwnUserException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleDeleteOwnUser() {
        return new ApiError(ApiErrorCode.DELETE_OWN_USER, "Cannot delete you own user");
    }

    @ExceptionHandler(ChangeOwnAccountActiveDatesException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleChangeOwnAccountActiveDates() {
        return new ApiError(ApiErrorCode.CHANGE_OWN_ACTIVE_DATES, "Cannot change your own user validity dates.");
    }

    @ExceptionHandler(ChangeOwnAccountStateException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ApiError handleChangeOwnAccountState() {
        return new ApiError(ApiErrorCode.CHANGE_OWN_ACCOUNT_STATE, "Cannot change your own user account state.");
    }
}
