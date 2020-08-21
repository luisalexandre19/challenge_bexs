package br.flight.bex.challenge.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChallengeException extends RuntimeException {

    private HttpStatus status;

    public ChallengeException(String message) {
       super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ChallengeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
