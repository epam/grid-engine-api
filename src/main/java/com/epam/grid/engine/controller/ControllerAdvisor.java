/*
 *
 *  * Copyright 2022 EPAM Systems, Inc. (https://www.epam.com/)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.epam.grid.engine.controller;

import com.epam.grid.engine.entity.job.HandleExceptionBody;
import com.epam.grid.engine.exception.GridEngineException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class ControllerAdvisor extends ResponseEntityExceptionHandler {
    private static final String LOGGING_MSG_HTTP_MESSAGE_NOT_READABLE = "{} {}: Input body parameters cannot be parsed";
    private static final String LOGGING_MSG_HTTP_MEDIA_TYPE_NOT_SUPPORTED = "{} {}: Unsupported media type";
    private static final String LOGGING_MSG_HTTP_REQUEST_METHOD_NOT_SUPPORTED
            = "{} {}: Http request method not supported";
    private static final String LOGGING_MSG_MISSING_PATH_VARIABLE = "{} {}: Missing path variable";

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> gridEngineExceptionHandler(final Throwable exception, final WebRequest request) {
        log.error(exception.getMessage(), exception);
        if (exception instanceof GridEngineException) {
            final GridEngineException gridEngineException = (GridEngineException) exception;
            return handleExceptionInternal(gridEngineException,
                    createBody(gridEngineException, gridEngineException.getHttpStatus()), new HttpHeaders(),
                    gridEngineException.getHttpStatus(), request);
        }
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Returns a ResponseEntity with detailed message in case of HttpMessageNotReadable exception,
     * which occurs in case of passing not-parsable parameters to the request.
     *
     * @param exception an HttpMessageNotReadable exception
     * @param headers   request headers
     * @param status    response status identified with HttpMessageNotReadable exception
     * @param request   web request details
     * @return a ResponseEntity containing the detailed message about the exception occurred
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException exception,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatus status,
                                                                  final WebRequest request) {
        final HttpServletRequest webRequest = ((ServletWebRequest) request).getRequest();
        log.error(LOGGING_MSG_HTTP_MESSAGE_NOT_READABLE, webRequest.getMethod(), webRequest.getRequestURI(), exception);
        return new ResponseEntity<>(exception.getMessage(), status);
    }

    /**
     * Returns a ResponseEntity with detailed message in case of HttpMediaTypeNotSupportedException exception,
     * which occurs in case of defining unsupported content type in request headers.
     *
     * @param exception an HttpMediaTypeNotSupportedException exception
     * @param headers   request headers
     * @param status    response status identified with HttpMediaTypeNotSupportedException exception
     * @param request   web request details
     * @return a ResponseEntity containing the detailed message about the exception occurred
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException exception,
                                                                     final HttpHeaders headers,
                                                                     final HttpStatus status,
                                                                     final WebRequest request) {
        final HttpServletRequest webRequest = ((ServletWebRequest) request).getRequest();
        log.error(LOGGING_MSG_HTTP_MEDIA_TYPE_NOT_SUPPORTED,
                webRequest.getMethod(),
                webRequest.getRequestURI(),
                exception);
        return new ResponseEntity<>(exception.getMessage(), status);
    }

    /**
     * Returns a ResponseEntity with detailed message in case of HttpRequestMethodNotSupportedException exception,
     * which occurs in case of trying to reach non-existent endpoint.
     *
     * @param exception an HttpRequestMethodNotSupportedException exception
     * @param headers   request headers
     * @param status    response status identified with HttpRequestMethodNotSupportedException exception
     * @param request   web request details
     * @return a ResponseEntity containing the detailed message about the exception occurred
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            final HttpRequestMethodNotSupportedException exception,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {
        final HttpServletRequest webRequest = ((ServletWebRequest) request).getRequest();
        log.error(LOGGING_MSG_HTTP_REQUEST_METHOD_NOT_SUPPORTED,
                webRequest.getMethod(),
                webRequest.getRequestURI(),
                exception);
        return new ResponseEntity<>(exception.getMessage(), status);
    }

    /**
     * Returns a ResponseEntity with detailed message in case of MissingPathVariableException exception,
     * which occurs in case there's path variable is unable to be found in request URL
     * (e.g. incorrect controller configuration).
     *
     * @param exception an MissingPathVariableException exception
     * @param headers   request headers
     * @param status    response status identified with MissingPathVariableException exception
     * @param request   web request details
     * @return a ResponseEntity containing the detailed message about the exception occurred
     */
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(final MissingPathVariableException exception,
                                                               final HttpHeaders headers,
                                                               final HttpStatus status,
                                                               final WebRequest request) {
        final HttpServletRequest webRequest = ((ServletWebRequest) request).getRequest();
        log.error(LOGGING_MSG_MISSING_PATH_VARIABLE, webRequest.getMethod(), webRequest.getRequestURI(), exception);
        return new ResponseEntity<>(exception.getMessage(), status);
    }

    private HandleExceptionBody createBody(final RuntimeException exception,
                                           final HttpStatus status) {
        return new HandleExceptionBody(LocalDateTime.now(), status, exception.getMessage());
    }
}
