package si.fri.ais.workspace.api.cli.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;
import si.fri.ais.workspace.core.exceptions.WorkspaceException;

@JBossLog
@ApplicationScoped
public class WorkspaceExceptionMapper implements CliExceptionMapper {
    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof WorkspaceException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        String message = switch (((WorkspaceException) exception).getCode()) {
            case OWNER_NOT_FOUND -> "The workspace owner was not found.";
            case NAME_NOT_UNIQUE -> "You already have a workspace with that name.";
            case WORKSPACE_NOT_FOUND -> "Workspace not found.";
            case DELETION_BLOCKED -> "Remove the workspace's remaining components before deleting it.";
        };
        log.error("Workspace command failed.", exception);
        command.getErr().println("Error: " + message);
        return CommandLine.ExitCode.SOFTWARE;
    }
}
