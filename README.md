# ais-savron-tisak

Repozitorij za zadeve vezane na AIS-DT projekt za študijsko leto 2026/2027

## Development patterns

Must yet be defined

## Usage

First you must package the app:

```shell
mvn clean package -Pcli
```

Then you can run the CLI via commands (use `--help` for mora info):

```shell
java -jar target/quarkus-app/quarkus-run.jar --help
```

## Native image

ArcadeDB library must be prepared for embadded mode based on their github repo, then it can be built
