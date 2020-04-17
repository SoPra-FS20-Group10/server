package ch.uzh.ifi.seal.soprafs20.exceptions;

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
import org.springframework.web.server.ResponseStatusException;
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

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseStatusException handleConflictException(ConflictException ex) {
        log.error(String.format("ConflictException raised: raised %s", ex));
        return new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseStatusException handleUnauthorizedException(UnauthorizedException ex) {
        log.error(String.format("LoginException raised: raised %s", ex));
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseStatusException handleNotFoundException(NotFoundException ex) {
        log.error(String.format("UpdateException raised: raised %s", ex));
        return new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
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