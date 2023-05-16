package xyz.peasfultown.dao;

public class RecordAlreadyExistsException extends DAOException {
    public RecordAlreadyExistsException() {
        super();
    }

    public RecordAlreadyExistsException(String message) {
        super(message);
    }

    public RecordAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
