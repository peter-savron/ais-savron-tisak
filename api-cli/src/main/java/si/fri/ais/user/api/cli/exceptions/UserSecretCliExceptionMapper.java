package si.fri.ais.user.api.cli.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;

@JBossLog
@ApplicationScoped
public class UserSecretCliExceptionMapper implements CliExceptionMapper {
    public static final String NAME_RULES =
            "Use 1 to 64 ASCII letters, digits, underscores, or hyphens, starting with a letter.";

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof UserSecretCliException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        log.info("Secret CLI command failed.", exception);
        command.getErr().println("Error: " + messageFor((UserSecretCliException) exception));
        return CommandLine.ExitCode.SOFTWARE;
    }

    public String messageFor(UserSecretCliException exception) {
        return switch (exception.getCode()) {
            case INVALID_USER_NAME -> "Invalid user name. " + NAME_RULES;
            case INVALID_SECRET_NAME -> "Invalid secret name. " + NAME_RULES;
            case USER_NOT_FOUND -> "The selected user was not found.";
            case LOGIN_REQUIRED -> "Log in with 'user login --name <user>' and retry.";
            case USER_REQUIRED -> "Supply --user or start a session for the secret's owner.";
            case NAME_REQUIRED -> "Supply --name to select a secret.";
            case VALUE_FILE_REQUIRED -> "Supply --value-file when a terminal is unavailable.";
            case BINARY_FILE_REQUIRED -> "Use --value-file with --type BYTES to read the raw bytes.";
            case VALUE_FILE_UNREADABLE -> "Cannot read the value file. Check the path and its permissions.";
            case INVALID_VALUE -> "The value must match its type: INTEGER uses whole digits; DECIMAL uses a decimal number, optionally with an exponent.";
            case INVALID_TEXT_ENCODING -> "TEXT, INTEGER, and DECIMAL values must use valid UTF-8.";
            case PASSWORD_REQUIRED -> "Supply --password when a terminal is unavailable.";
            case INVALID_PASSWORD -> "The password must contain 8 to 128 ASCII letters, digits, or punctuation.";
            case SECRET_NOT_FOUND -> "The selected secret was not found for this user.";
            case OUTPUT_FAILURE -> "Cannot write the secret to the selected output.";
        };
    }
}
