package ch.uzh.ifi.seal.soprafs20.exceptions.UserServiceExceptions;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;

public class LoginException extends SopraServiceException {

    public LoginException(String message) {
        super(message);
    }
}
