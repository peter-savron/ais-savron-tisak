---
name: api-cli
description: Info how to build a scalable cli interface. 
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

Similarily each feature has some actions, implement in action separating implementing `FeatureCliAction` inteface where `Feature` in this name should be replaced by the actual feature name like `UserCliAction`.

### Command parameters

Command parameters are defined via the use of `quarkus-picocli` invocations.

### Prompt user

TODO sometimes you would like the user to specify some commands later

### Exception handling

First you should have a feature specific exception mapper to use in catch blocks, if it does not recognize the exception it should be forwarded to the common exception mapper. Exceptions should be displayed to the User in a friendly textual way, stacktraces should not escape to the terminal, but be only stored in file logs.

### Calling core services

You should map command line parameters to core models when invoking core services. First round of validation should be done in the resource, even if you could rely on core validation. In most cases you should call only one service and one of its methods at a time. User session should be resolved at the Cli command level, if needed a SessionService could be used to resolve them.

### Error handling

A mapper for a specific exception should implement `CliExceptionMapper.class`. Exception types that are related only to CLI execution (prompt the Master for clarification) should be implemented in the `api` pacakge, most exceptions should be specified in the core.