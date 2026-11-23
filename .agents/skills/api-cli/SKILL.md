---
name: api-cli
description: Implement or refactor this project's Quarkus CLI actions with Picocli options, session defaults, and JLine 4 prompts.
---

# CLI API

The CLI commands are implemented via `quarkus-picocli`. The structure of a command should follow this pattern: `app-name feature action --options`.

## Naming convention

Main command then has different features. Each feature implementation implements `CliFeature.class` and is named `{FeatureName}CliFeature` each feature could provide many actions over the features data. Interfaces which feature actions should implement are called `{FeatureName}CliAction` to distinguish them. Each feature action implementation class should be called `{FeatureName}{ActionName}CliAction`. CLI actions are stored in the actions subpackage.

## Implementation

Implement each action separately so class remain of modest size and specify all app features via injection like:  

```java
@QuarkusMain
public class MainApplication implements QuarkusApplication {
    private final CommandLine.IFactory factory;
    private final MainCommand mainCommand;
    private final Instance<CliFeature> cliFeatures;

    public MainApplication(CommandLine.IFactory factory, MainCommand mainCommand, Instance<CliFeature> cliFeatures) {
        this.factory = factory;
        this.mainCommand = mainCommand;
        this.cliFeatures = cliFeatures;
    }

    @Override
    public int run(String... args) {
        // Setup command line
        CommandLine cmd = new CommandLine(mainCommand, factory);
        cliFeatures.stream().forEach(cmd::addSubcommand);

        return cmd.execute(args);
    }
}
```

Similarly each feature has some actions, implement in action separating implementing `FeatureCliAction` interface where `Feature` in this name should be replaced by the actual feature name like `UserCliAction`.

Read the existing actions for the feature before adding another. Follow their command structure, constructor injection, and terminal conventions. Keep reference actions intact unless the task calls for changing them. Each concrete action implements its feature action interface directly and owns its `run()` method.

### Command parameters

Command parameters are defined via the use of `quarkus-picocli` `@Command.Option`.

Declare Picocli options directly on the concrete action. Use an action-local `@ArgGroup` for mutually exclusive selectors. Avoid `@CommandLine.Mixin` and inherited option declarations for sharing command flags.

Resolve explicit selectors first, then use the current session where applicable. Keep a flag optional when session context or an interactive prompt can supply its value. When an action requires an explicit selector, accept it through the option or prompt and skip session defaults. Resolve and validate the selector before prompting for a password or calling the core service.

### CLI option resolvers

Use a feature-level resolver when actions share option validation, session fallback, or prompting. Make the resolver an `@ApplicationScoped` CDI bean. Constructor-inject the `LineReader`, prompts, and other dependencies it needs. Actions inject the resolver and pass option values to it. Keep per-invocation values local to each call and declare Picocli options on the actions.

### Prompt user

You specify a user prompt when he needs to specify a secret or to pass a long text or make a guided selection. Use JLine 4 for prompts.

Store reusable CLI prompts in `si.fri.ais.common.api.cli.prompts`. Add a separate class for each distinct reusable prompt and keep feature-specific decisions in the action that calls it.

Make reusable prompts that own a JLine dependency `@ApplicationScoped` CDI beans. Constructor-inject the `LineReader` into `PasswordPrompt` and inject the prompt into each action that uses it. Keep input and validation state local to each call.

Use `LinePrompt` for unmasked text. Apply feature-specific validation and retries at the call site. Let EOF and cancellation reach the CLI exception mapper.

Use the injected JLine 4 input component that matches the feature's existing prompt pattern. For a single masked password, follow the registration action. Inject the project's `org.jline.reader.LineReader` and call `readLine(message, '*')`. Disable history while reading credentials, restore the previous setting in `finally`, and flush the terminal. Use the `jline-prompt` builders for commands designed around a structured prompt flow.

For new actions that accept a password option, prompt only when no value was provided. Validate supplied and prompted values against the operation's requirements. Retry invalid interactive input, route cancellation and EOF through the CLI exception mapper, and clear mutable password buffers after use. Verify interactive behavior through the packaged CLI in a terminal.

### Exception handling

Default to one `CliExceptionMapper` per exception type. A mapper may translate multiple enum codes carried by its exception type. Put feature-specific mappers in the feature's CLI package and shared CLI mappers in the common CLI package. Let the shared resolver dispatch exceptions and use the fallback mapper for unrecognized types. Print friendly messages to the terminal and keep stack traces in file logs.

### Calling core services

You should map command line parameters to core models when invoking core services. First round of validation should be done in the resource, even if you could rely on core validation. In most cases you should call only one service and one of its methods at a time. User session should be resolved at the Cli command level, if needed a SessionService could be used to resolve them.

### Error handling

A mapper for a specific exception should implement `CliExceptionMapper.class`. Exception types that are related only to CLI execution (prompt the Master for clarification) should be implemented in the `api` pacakge, most exceptions should be specified in the core.
