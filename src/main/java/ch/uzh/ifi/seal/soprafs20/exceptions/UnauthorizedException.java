package ch.uzh.ifi.seal.soprafs20.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class UnauthorizedException extends SopraServiceException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
