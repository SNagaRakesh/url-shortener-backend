package com.BEProjects.url_shortener.web;

import com.BEProjects.url_shortener.domain.exceptions.ShortUrlNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class globalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(globalExceptionHandler.class);

    @ExceptionHandler(ShortUrlNotFoundException.class)
    String handleShortUrlNotFoundException (ShortUrlNotFoundException ex) {
        logger.error("Short URL Not Found: {} ", ex.getMessage());
        return "errors/404";
    }

    @ExceptionHandler(Exception.class)
    String handleException (Exception ex) {
        logger.error("Unhandled Exception: {}", ex.getMessage(), ex);
        return "errors/500";
    }
}
