package si.fri.ais.common.api.cli.exceptions;

import picocli.CommandLine;

public interface CliExceptionMapper {
    /**
     * Specifies whether this mapper can handle the specified error.
     * @param t error thrown.
     * @return true if it can handle the throwable.
     */
    boolean canHandle(Throwable t);

    /**
     * Handle the error.
     *
     * @param t error thrown
     * @param cmd
     * @param parseResult
     * @return app exit code
     */
    int handle(Throwable t, CommandLine cmd, CommandLine.ParseResult parseResult);

    /**
     * Lower values indicate higher priority.
     * Specific mappers should override with lower values,
     * while catch-all mappers use higher values (e.g., Integer.MAX_VALUE).
     */
    default int priority() {
        return 1000;
    }
}