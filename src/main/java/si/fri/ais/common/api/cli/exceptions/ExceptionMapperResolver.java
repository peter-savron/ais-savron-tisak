package si.fri.ais.common.api.cli.exceptions;

import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class ExceptionMapperResolver implements CommandLine.IExecutionExceptionHandler {

    private final List<CliExceptionMapper> sortedMappers;

    public ExceptionMapperResolver(@All List<CliExceptionMapper> mappers) {
        this.sortedMappers = mappers.stream()
                .sorted(Comparator.comparingInt(CliExceptionMapper::priority))
                .toList();
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) throws Exception {
        for (CliExceptionMapper mapper : sortedMappers) {
            if (mapper.canHandle(ex)) {
                return mapper.handle(ex, cmd, parseResult);
            }
        }

        // No mapper claimed the exception: let Picocli rethrow it
        throw ex;
    }
}
