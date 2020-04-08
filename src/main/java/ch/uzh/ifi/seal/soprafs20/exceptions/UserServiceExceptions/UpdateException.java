package ch.uzh.ifi.seal.soprafs20.exceptions.UserServiceExceptions;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;

public class UpdateException extends SopraServiceException {

    public UpdateException(String message) {
        super(message);
    }
}
