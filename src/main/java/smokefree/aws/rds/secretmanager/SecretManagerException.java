package smokefree.aws.rds.secretmanager;

public class SecretManagerException extends RuntimeException {

    public SecretManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
