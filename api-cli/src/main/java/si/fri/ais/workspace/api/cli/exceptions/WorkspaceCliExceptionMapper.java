package si.fri.ais.workspace.api.cli.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;
import si.fri.ais.workspace.core.models.Workspace;

import static si.fri.ais.common.core.utils.MainApplicationValues.MAX_PASSWORD_LENGTH;
import static si.fri.ais.common.core.utils.MainApplicationValues.MIN_PASSWORD_LENGTH;

@JBossLog
@ApplicationScoped
public class WorkspaceCliExceptionMapper implements CliExceptionMapper {
    public static final String NAME_RULES =
            "Use 1 to 64 ASCII letters, digits, underscores, or hyphens, starting with a letter.";

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof WorkspaceCliException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        WorkspaceCliException failure = (WorkspaceCliException) exception;
        String message = switch (failure.getCode()) {
            case INVALID_NAME -> "Invalid %s. %s".formatted(failure.getReference(), NAME_RULES);
            case USER_NOT_FOUND -> "User %s was not found.".formatted(failure.getReference());
            case LOGIN_REQUIRED -> "Run 'user login --name %s' and retry.".formatted(failure.getReference());
            case NAME_REQUIRED_FOR_CREATE -> "Supply --name to create a workspace.";
            case NAME_REQUIRED_FOR_SELECTION -> "Supply --name to select a workspace.";
            case NO_WORKSPACES -> "You have no workspaces to select.";
            case EDIT_REQUIRED -> "Supply at least one of --new-name, --title, or --description.";
            case DESCRIPTION_TOO_LONG -> "Workspace descriptions support at most %d characters."
                    .formatted(Workspace.DESCRIPTION_LIMIT);
            case PASSWORD_REQUIRED -> "Supply --password when a terminal is unavailable.";
            case INVALID_PASSWORD_OPTION -> "Use %d to %d ASCII letters, digits, or punctuation for --password."
                    .formatted(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
        };
        log.info("Workspace CLI command failed.", exception);
        command.getErr().println("Error: " + message);
        return CommandLine.ExitCode.SOFTWARE;
    }
}
