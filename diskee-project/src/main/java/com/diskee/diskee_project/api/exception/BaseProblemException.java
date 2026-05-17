// com/diskee/diskee_project/api/exception/BaseProblemException.java
package com.diskee.diskee_project.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;

public abstract class BaseProblemException extends ErrorResponseException {

    protected BaseProblemException(HttpStatus status, String type, String title, String detail) {
        super(status, createProblemDetail(status, type, title, detail), null);
    }

    private static ProblemDetail createProblemDetail(HttpStatus status, String type, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(type));
        pd.setTitle(title);
        return pd;
    }
}