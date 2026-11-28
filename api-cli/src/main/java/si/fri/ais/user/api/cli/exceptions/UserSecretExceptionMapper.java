package si.fri.ais.user.api.cli.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;
import si.fri.ais.user.core.exceptions.UserSecretException;

@JBossLog
@ApplicationScoped
public class UserSecretExceptionMapper implements CliExceptionMapper {
    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof UserSecretException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        UserSecretException failure = (UserSecretException) exception;
        String message = switch (failure.getCode()) {
            case OWNER_NOT_FOUND -> "The secret's owner was not found.";
            case USER_LOGGED_OUT -> "Log in with 'user login --name <user>' and retry.";
            case KEY_UNAVAILABLE -> "The user's encryption key is unavailable. Log in again and retry.";
            case INVALID_PASSWORD -> "The password is incorrect.";
            case SECRET_NOT_FOUND -> "The selected secret was not found for this user.";
            case NAME_NOT_UNIQUE -> "This user already has a secret with that name.";
            case ENCRYPTION_FAILED -> "Could not encrypt the secret. Log in again and retry.";
            case SECRET_INTEGRITY_FAILURE -> "The secret could not be authenticated. Check the stored key and encrypted data.";
            case UNSUPPORTED_FORMAT -> "The secret uses an unsupported encryption format.";
            case STORAGE_FAILURE -> "Could not access secret storage. Check the application log and retry.";
        };
        switch (failure.getCode()) {
            case ENCRYPTION_FAILED, STORAGE_FAILURE -> log.error("Secret command failed.", exception);
            default -> log.info("Secret command failed.", exception);
        }
        command.getErr().println("Error: " + message);
        return CommandLine.ExitCode.SOFTWARE;
    }
}
