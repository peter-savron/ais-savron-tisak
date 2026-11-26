---
name: core
description: Implement app core functionalities
---

# Core logic

## Useful dependencies

- `apache-lang-3`

## Models

One model for each operation input (Action) - Action inputs should be annotated with `@SuperBuilder` `@NoArgsConstructor` and `@Getter` so they are immutable.
Create a common core context model for each feature to group the identifiers shared by its operations. Embed it in action inputs with cascading validation. When the context is the entire input, pass it directly.

One model per feature - well documented - model should be annotated with `@SuperBuilder` `@Getter` and `@Setter`.

When creating records, add them `@Builder` from Lombok, create them via builder instead of all args constructor for readability purposes.

All well validated

Avoid duplicating Jakarta Bean Validation constraints in constructors. Use cascading validation at the service boundary for constrained input models.

Model fields should be documented with Javadocs for clarity, give examples for values and usage.

Specs might underdefine model fields. If there are fields missing that are relevant for core functionalities, a spec change should be proposed to the Master, and model fields should be extended to include relevant data.

## Interfaces

Each interface should be documented to know how to implement it. Each method should have matching Javadocs which describe how behaves, expects and returns.

Do not throw generic Java exceptions from core interfaces. When implementing a core interface, implement feature-specific exceptions with stable codes for each failure reason so issues are trackable. Document the failures on the interface and preserve the cause when translating an underlying exception.

### Repositories

All write methods in repository should return `void`, query operations should return either `Optional` or `List`. Update interfaces should take just one parameter which will be updated in place.

Repository interface should specify `get` (multiple if there are multiple possible identifiers), `list`, `query`, `create`, `update`, `delete` methods. Other methods should not be added unless there is a quite specific need for it specified by the master.

All authorization is done in the core, so only data required for the read/write operation should be passed. 

## Core implementations

Assume that all required CDI beans will be provided by the time the application build is triggered. Do not add build properties or profiles to work around missing beans during implementation.

Expose a domain-specific for utils, depending on context it could be either checked or unchecked, use checked Exceptions when caller should be aware that he should handle negative scenarios. Translate underlying I/O failures at the processing boundary and preserve their causes.

Inject repository interfaces directly through service constructors. Do not wrap repository dependencies in CDI `Instance<T>`, unless there is a valid reason to search for correct instance.

Single-record lookups exposed to other features should return `Optional` when a record may be absent. When an operation requires the record, the calling feature should use `orElseThrow` with its own exception.

Implementations of `{interfaceName}` should follow the naming patter `{interfaceName}{optional:implementationKind}Impl`.

You should always update models in place and never create models copies.

You may call another feature Service in the implementation if it is required, but you should never throw exceptions related to another feature inside the core logic for your own feature. All core feature rules should be encompassed in the core feature implementation.
