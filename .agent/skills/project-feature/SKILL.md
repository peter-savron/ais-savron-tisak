---
name: project-feature
description: Implement or refactor Java features in this project's Quarkus services, ports, models, and Picocli adapters while preserving its package and dependency boundaries.
---

# Implement a project feature

Read the root `AGENTS.md` and the relevant feature document in `docs/features`.
Trace the actual caller, service port, implementation, repository port, and adapter
before deciding what to change. Use the User feature as a layout reference, not
as a requirement to copy every current implementation choice or unfinished TODO.

## Keep the change in the right layer

- CLI adapters parse input, invoke a service, and present results. They must not
  query ArcadeDB, calculate password hashes, or implement business rules.
- Service contracts belong in `core/ports/outgoing`; implementations in
  `core/ports/outgoing/impl/local` orchestrate repository ports and own business
  checks. Inject collaborators through constructors, with final dependency fields.
- Repository contracts belong in `core/ports/incoming`; database and filesystem
  details belong in `integrations/local`. Services use domain objects, not
  ArcadeDB documents or result sets.
- Reuse `NamedObject`/`UnnamedObject`, Lombok builders/getters, and command objects
  under the existing `core/models/commands` package where they fit. Keep helpers
  local until multiple real callers justify sharing them under `common`.
- Use `@ApplicationScoped` for managed implementations and
  `@IfBuildProfile("cli")` for CLI-only services. Check CDI selection when adding
  a second implementation of a port.

## Define behavior before filling a stub

Identify success, absence, invalid input, duplicates, and collaborator failure
behavior for the requested operation. Preserve `Optional` lookup results and the
existing return contract. Add Jakarta constraints to port/command boundaries in
the existing style; remember that plain unit construction does not execute CDI
method validation. Do not redeclare inherited method constraints on overrides.

Perform credential/authorization checks before destructive side effects. Propagate
failures with useful context without exposing credentials. Keep multi-step storage
changes recoverable; use the persistence skill when transactions or cleanup matter.
Do not silently return null or an empty collection for an unsupported operation.
Do not implement unrelated authentication, secret migration, or session TODOs just
because a nearby method is unfinished.

For credentials, use the existing Argon2id implementation, independent password
and key salts, and `UserKey` lifecycle. Stored password hashes are not AES keys.
Changing AES derivation settings affects existing encrypted data and requires an
explicit migration design.

Use the project-tests skill for focused behavior tests. Update feature docs when
observable behavior or compatibility changes. Finish by reviewing the diff for
boundary violations and unrelated changes, then run the relevant Maven checks
listed in `AGENTS.md`.
