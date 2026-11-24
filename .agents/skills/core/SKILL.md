---
name: core
description: Implement app core functionalities
---

# Core logic

## Useful dependencies

- `apache-lang-3`

## Models

One model for each operation input (Action) - Action inputs should be annotated with `@SuperBuilder` `@NoArgsConstructor` and `@Getter` so they are immutable.

One model per feature - well documented - model should be annotated with `@SuperBuilder` `@Getter` and `@Setter`.

All well validated

## Interfaces

### Repositories

All write methods in repository should return `void`, query operations should return either `Optional` or `List`. Update interfaces should take just one parameter which will be updated in place.

## Core implementations

Implementations of `{interfaceName}` should follow the naming patter `{interfaceName}{optional:implementationKind}Impl`.

You should always update models in place and never create models copies.