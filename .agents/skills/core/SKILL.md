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

Single-record lookups exposed to other features should return `Optional` when a record may be absent. When an operation requires the record, the calling feature should use `orElseThrow` with its own exception.

Implementations of `{interfaceName}` should follow the naming patter `{interfaceName}{optional:implementationKind}Impl`.

You should always update models in place and never create models copies.

You may call another feature Service in the implementation if it is required, but you should never throw exceptions related to another feature inside the core logic for your own feature. All core feature rules should be encompassed in the core feature implementation.
