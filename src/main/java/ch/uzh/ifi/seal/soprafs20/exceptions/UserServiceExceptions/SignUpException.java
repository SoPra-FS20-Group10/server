package ch.uzh.ifi.seal.soprafs20.exceptions.UserServiceExceptions;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;

public class SignUpException extends SopraServiceException {
    public SignUpException(String message) {
        super(message);
    }
}