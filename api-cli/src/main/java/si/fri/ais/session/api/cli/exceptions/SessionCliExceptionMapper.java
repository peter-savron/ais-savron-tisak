package si.fri.ais.session.api.cli.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;
import si.fri.ais.session.core.exceptions.SessionException;

@JBossLog
@ApplicationScoped
public class SessionCliExceptionMapper implements CliExceptionMapper {
    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof SessionCliException || exception instanceof SessionException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        if (exception instanceof SessionCliException cliException) {
            log.info("Session command failed.", cliException);
            command.getErr().println(messageFor(cliException));
            return CommandLine.ExitCode.SOFTWARE;
        }

        SessionException session = (SessionException) exception;
        String reference = session.getReference();
        String message = switch (session.getCode()) {
            case SLOT_OCCUPIED -> "The session is already active.";
            case SESSION_INACTIVE -> "The session is inactive.";
            case LINKED_SESSION_INACTIVE -> "The selected session is unavailable.";
            case NAMED_SESSION_MISSING -> "Named session %s was not found.".formatted(reference);
            case USER_MISSING -> "The session user was not found.";
            case LOGIN_REQUIRED -> "Run 'user login --name %s' and retry.".formatted(reference);
            case WORKSPACE_UNAVAILABLE -> "The session workspace is unavailable.";
            case PREFERENCES_UNCHANGED -> "Supply a different user or workspace.";
            case CONCURRENT_CHANGE -> "The session changed during the operation. Retry the command.";
        };
        log.info("Session command failed.", exception);
        command.getErr().println(message);
        return CommandLine.ExitCode.SOFTWARE;
    }

    public static String messageFor(SessionCliException exception) {
        return switch (exception.getCode()) {
            case SHELL_UNAVAILABLE -> "Cannot identify the invoking shell.";
            case INVALID_SESSION_NAME -> "Session names must start with an ASCII letter and contain at most 64 letters, digits, underscores or hyphens.";
            case NO_NAMED_SESSIONS -> "No named sessions are available.";
            case USER_REQUIRED -> "Supply --user.";
            case USER_OR_WORKSPACE_REQUIRED -> "Supply --user or --workspace.";
            case SESSION_NAME_REQUIRED -> "Supply --global or --name.";
            case SESSION_TARGET_REQUIRED -> "Supply --local, --global or --name.";
            case WORKSPACE_LOOKUP_UNAVAILABLE -> "Workspace name lookup is unavailable.";
            case INVALID_USER_NAME -> "Enter a valid user name.";
            case USER_NOT_FOUND -> "User %s was not found.".formatted(exception.getReference());
            case SESSION_INACTIVE -> "The session is inactive.";
            case PREFERENCES_UNCHANGED -> "Supply a different user or workspace.";
        };
    }
}
