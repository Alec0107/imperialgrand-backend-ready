package com.imperialgrand.backend.common.globalexception;

import com.imperialgrand.backend.contact_us.exception.InternalServerError;
import com.imperialgrand.backend.email.exception.EmailAlreadyVerifiedException;
import com.imperialgrand.backend.email.exception.EmailTokenException;
import com.imperialgrand.backend.email.exception.EmailTokenExpiredException;
import com.imperialgrand.backend.jwt.exception.InvalidJwtTokenException;
import com.imperialgrand.backend.jwt.exception.MissingRefreshTokenException;
import com.imperialgrand.backend.jwt.exception.RefreshTokenExpiredException;
import com.imperialgrand.backend.jwt.exception.WrongTypeTokenException;
import com.imperialgrand.backend.reservation.exception.NoAvailableTableException;
import com.imperialgrand.backend.reservation.exception.ReservationLockNotFoundException;
import com.imperialgrand.backend.reservation.exception.ReservationNoLongerHeldException;
import com.imperialgrand.backend.resetpassword.exception.InvalidResetPasswordTokenException;
import com.imperialgrand.backend.resetpassword.exception.TokenExpiredException;
import com.imperialgrand.backend.common.response.ErrorResponse;
import com.imperialgrand.backend.user.exception.EmailAlreadyUsedException;
import com.imperialgrand.backend.user.exception.EmailNotFoundException;
import com.imperialgrand.backend.user.exception.EmailNotVerifiedException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CONFLICT_ERROR = "CONFLICT";
    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String UNAUTHORIZED = "UNAUTHORIZED";
    private static final String NOT_FOUND= "NOT_FOUND";
    private static final String TOO_MANY_REQUEST = "Too many requests";
    private static final String INTERNA= "UNAUTHORIZED";


    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyUsedException(EmailAlreadyUsedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                         ex.getMessage(),
                         CONFLICT_ERROR,
                         HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                         ex.getMessage(),
                         BAD_REQUEST,
                         HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid email or password", // keep it vague for security
                 UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value()); // Use 401 Unauthorized
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(InternalServerError ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(), // keep it vague for security
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()); // Use 401 Unauthorized
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

//    @ExceptionHandler(EmailTokenException.class)
//    public ResponseEntity<ErrorResponse> handleEmailTokenNotFoundException(EmailTokenException ex) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                         ex.getMessage(),
//                         BAD_REQUEST,
//                         HttpStatus.BAD_REQUEST.value());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                        ex.getMessage(),
                        BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotFoundException(EmailNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                NOT_FOUND,
                HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        System.out.println("ERRRORRR::: " + ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }


    @ExceptionHandler(CooldownException.class)
    public ResponseEntity<ErrorResponse> handleCooldownException(CooldownException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                TOO_MANY_REQUEST,
                HttpStatus.TOO_MANY_REQUESTS.value());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    /** EXCEPTION FOR RESENDING EMAIL VERIFICATION **/
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                BAD_REQUEST, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyVerifiedException(EmailAlreadyVerifiedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }



    /** EXCEPTION FOR VERIFYING EMAIL VERIFICATION TOKEN **/
    @ExceptionHandler(EmailTokenException.class)
    public RedirectView handleEmailTokenException(EmailTokenException ex) {
        String url = "http://127.0.0.1:5500/pages/user-inbox-email-response/email-error.html?status=" + ex.getStatus();
        System.out.println("URL: " + url);
        return new RedirectView(url);
    }

    @ExceptionHandler(EmailTokenExpiredException.class)
    public RedirectView handleEmailTokenException(EmailTokenExpiredException ex) {
        String url = "http://127.0.0.1:5500/pages/user-inbox-email-response/email-error.html?status=" + ex.getStatus() + "&tokenId=" + ex.getTokenId();
        System.out.println("URL: " + url);
        return new RedirectView(url);
    }


    /** EXCEPTION FOR RESETTING PASSWORD **/
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                "TOKEN_EXPIRED",
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidResetPasswordTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResetPasswordTokenException(InvalidResetPasswordTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                "INVALID_RESET_TOKEN",
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }




    // JWT TOKEN EXCEPTION
    @ExceptionHandler(MissingRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleMissingRefreshToken(MissingRefreshTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                "MISSING_REFRESH_TOKEN",
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(WrongTypeTokenException.class)
    public ResponseEntity<ErrorResponse> handleWrongTypeTokenException(WrongTypeTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                "WRONG_TYPE_JWT",
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpiredException(RefreshTokenExpiredException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                "EXPIRED_REFRESH_TOKEN",
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwtTokenException(InvalidJwtTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                "INVALID_JWT_TOKEN",
                HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }



    /**
     * EXCEPTIONS: FOR RESERVATION
     * **/
    @ExceptionHandler(NoAvailableTableException.class)
    public ResponseEntity<ErrorResponse> handleNoAvailableTableException(NoAvailableTableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "NO_TABLE_AVAILBALE",
                HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    @ExceptionHandler(ReservationLockNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationLockNotFoundException(ReservationLockNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                "RESERVATION_LOCK_NOT_FOUND",
                HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ReservationNoLongerHeldException.class)
    public ResponseEntity<ErrorResponse> handleReservationNoLongerHeldException(ReservationNoLongerHeldException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                "RESERVATION_LOCK_NO_LONGER_HELD",
                HttpStatus.GONE.value());
        return ResponseEntity.status(HttpStatus.GONE).body(errorResponse);
    }
}