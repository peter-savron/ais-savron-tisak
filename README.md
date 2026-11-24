# ais-savron-tisak

Repozitorij za zadeve vezane na AIS-DT projekt za študijsko leto 2026/2027

## Development patterns

Must yet be defined

## Build the CLI

Install JDK 21 and Maven 3.9. From the repository root, build the CLI and the modules it depends on.

```shell
mvn -pl api-cli -am clean package
```

## Run the CLI

The build puts the application in `api-cli/target/quarkus-app/`. Keep that directory together if you move or distribute the application. To see the available commands, run the following from the repository root.

```shell
java -jar api-cli/target/quarkus-app/quarkus-run.jar --help
```

## Native image

ArcadeDB library must be prepared for embadded mode based on their github repo, then it can be built
