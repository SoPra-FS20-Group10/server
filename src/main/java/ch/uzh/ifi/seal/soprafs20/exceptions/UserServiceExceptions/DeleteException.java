package ch.uzh.ifi.seal.soprafs20.exceptions.UserServiceExceptions;

import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;

public class DeleteException extends SopraServiceException {
    public DeleteException(String message) {
        super(message);
    }
}
