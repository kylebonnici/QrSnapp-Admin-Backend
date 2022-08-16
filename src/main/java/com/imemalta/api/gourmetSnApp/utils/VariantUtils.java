package com.imemalta.api.gourmetSnApp.utils;

import com.imemalta.api.gourmetSnApp.exceptions.authentication.NoUserInSessionException;
import com.imemalta.api.gourmetSnApp.dtos.ApiError;
import com.imemalta.api.gourmetSnApp.dtos.ApiErrorCode;
import com.imemalta.api.gourmetSnApp.exceptions.common.ConstraintViolationException;
import com.imemalta.api.gourmetSnApp.services.SystemConfiguration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class VariantUtils {
    public static boolean hasDurationElapsed(LocalDateTime currentDateTime, long durationInSeconds, LocalDateTime date){
        return Duration.between(date, currentDateTime).toSeconds() > durationInSeconds;
    }

    public static boolean isPasswordUnsafe(String password){
        return password == null || !password.matches(SystemConfiguration.passwordRegex);
    }

    public static String currentSessionUsername(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        } else {
            throw new NoUserInSessionException();
        }
    }

    public static <T> Set<ConstraintViolation<T>> validateConstraints(T object) {
        return validateConstraints(object, null);
    }

    public static <T> Set<ConstraintViolation<T>> validateConstraints(T object, String[] filters) {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();

        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object);

        if (filters != null && filters.length > 0) {
            List<String> listFilters = Arrays.asList(filters);

            constraintViolations = constraintViolations.stream().filter(cv ->
                    !listFilters.contains(cv.getPropertyPath().toString())).collect(Collectors.toSet());
        }

        return constraintViolations;
    }

    public static <T> void throwConstraintViolations(Set<ConstraintViolation<T>> constraintViolations){
        if (constraintViolations.size() > 0){
            ApiError apiError = new ApiError(ApiErrorCode.CONSTRAIN_VIOLATION, "Some fields failed to pass to constraints. View meta data for more info");

            constraintViolations.forEach(cv -> {
                if (cv.getPropertyPath() != null ) {
                    String key = cv.getPropertyPath().toString();
                    String invalidValue = cv.getInvalidValue() == null? "null" : cv.getInvalidValue().toString();
                    if (apiError.getMetaData().containsKey(key)) {
                        apiError.getMetaData().put(key, apiError.getMetaData().get(key).toString() + ";" + invalidValue + ": " + cv.getMessage());
                    } else {
                        apiError.getMetaData().putIfAbsent(key, invalidValue + ": " + cv.getMessage());
                    }
                }
            });

            throw new ConstraintViolationException(apiError);
        }
    }
}
