package br.flight.bex.challenge.exceptions.handler;

import br.flight.bex.challenge.exceptions.ChallengeException;
import br.flight.bex.challenge.exceptions.validation.ValidationError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<Object> handleClientErrorException(HttpClientErrorException exception, WebRequest request){
        return new ResponseEntity<>(
                exception.getStatusText(),
                exception.getResponseHeaders(),
                exception.getStatusCode());
    }

    @ExceptionHandler({HttpServerErrorException.class})
    public ResponseEntity<Object> handleServerErrorException(HttpServerErrorException exception, WebRequest request){
        return new ResponseEntity<>(
                exception.getStatusText(),
                exception.getResponseHeaders(),
                exception.getStatusCode());
    }

    @ExceptionHandler(value = ChallengeException.class)
    public ResponseEntity<String> handleChallengeException(ChallengeException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList());

        return super.handleExceptionInternal(exception, ValidationError.builder().errors(errors).build(), headers, status, request);
    }

}