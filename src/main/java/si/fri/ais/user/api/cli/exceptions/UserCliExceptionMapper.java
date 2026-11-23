package si.fri.ais.user.api.cli.exceptions;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;
import si.fri.ais.user.core.exceptions.UserException;

@JBossLog
@IfBuildProfile("cli")
@ApplicationScoped
public class UserCliExceptionMapper implements CliExceptionMapper {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof UserException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        UserException userException = (UserException) exception;
        String message = switch (userException.getCode()) {
            case USER_CREATE_NAME_NOT_UNIQUE -> "A user with that name already exists.";
        };
        log.debug("User command failed", exception);
        command.getErr().println("Error: " + message);
        return CommandLine.ExitCode.SOFTWARE;
    }
}
