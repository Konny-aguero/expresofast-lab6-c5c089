package cr.ac.ucr.paraiso.ie.c5c089.lab5.exception;

import java.util.Map;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<?> error(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("status",status.value(),"error",mensaje));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        Map<String,String> fields=new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.put(e.getField(),e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("status",400,"error","Revise los campos del formulario","fields",fields));
    }
    @ExceptionHandler({InvalidStateTransitionException.class, IllegalArgumentException.class})
    public ResponseEntity<?> business(RuntimeException ex) { return error(HttpStatus.BAD_REQUEST,ex.getMessage()); }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> missing(ResourceNotFoundException ex) { return error(HttpStatus.NOT_FOUND,ex.getMessage()); }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> auth(AuthenticationException ex) { return error(HttpStatus.UNAUTHORIZED,"Credenciales inválidas o usuario inactivo"); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> forbidden(AccessDeniedException ex) { return error(HttpStatus.FORBIDDEN,"No tiene permisos para esta acción"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflict(DataIntegrityViolationException ex) { return error(HttpStatus.CONFLICT,"El registro está duplicado o tiene datos relacionados que impiden la operación"); }
    @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
    public ResponseEntity<?> malformed(Exception ex) { return error(HttpStatus.BAD_REQUEST,"La solicitud contiene datos inválidos"); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> route(Exception ex) { return error(HttpStatus.NOT_FOUND,"Recurso no encontrado"); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception ex) {
        log.error("Error inesperado al procesar la solicitud",ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,"Ocurrió un error inesperado en el servidor");
    }
}
