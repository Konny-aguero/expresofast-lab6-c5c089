package cr.ac.ucr.paraiso.ie.c5c089.lab5.exception;


public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}