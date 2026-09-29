package com.TheOptimizer.KisanFinTech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> validation(MethodArgumentNotValidException ex){
        Map<String,Object> out=new LinkedHashMap<>(); out.put("error","Invalid request");
        out.put("message",ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList()); return out;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,Object> notFound(IllegalArgumentException ex){return Map.of("error","Unable to process location","message",ex.getMessage());}
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String,Object> external(Exception ex){return Map.of("error","Recommendation pipeline failed","message",ex.getMessage()==null?"Unexpected error":ex.getMessage());}
}
