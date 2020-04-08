package ch.uzh.ifi.seal.soprafs20.exceptions;

import ch.uzh.ifi.seal.soprafs20.exceptions.UserServiceExceptions.LoginException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UserServiceExceptions.SignUpException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UserServiceExceptions.UpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "This should be application specific";
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(SopraServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SopraServiceException handleBadRequestException(SopraServiceException ex) {
        log.error(String.format("SopraServiceException raised:%s", ex));
        return ex;
    }

    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public void handleTransactionSystemException(Exception ex, HttpServletRequest request) {
        log.error(String.format("Request: %s raised %s", request.getRequestURL(), ex));
    }

    @ExceptionHandler(SignUpException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public SignUpException handleSignUpException(SignUpException ex) {
        log.error(String.format("SignUpException raised: raised %s", ex));
        return ex;
    }

    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public LoginException handleLoginException(LoginException ex) {
        log.error(String.format("LoginException raised: raised %s", ex));
        return ex;
    }

    @ExceptionHandler(UpdateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public UpdateException handleUpdateException(UpdateException ex) {
        log.error(String.format("UpdateException raised: raised %s", ex));
        return ex;
    }

    // Keep this one disable for all testing purposes -> it shows more detail with this one disabled
    /*
    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Exception handleException(Exception ex) {
        log.error(String.format("Exception raised:%s", ex));
        return ex;
    }

     */
}