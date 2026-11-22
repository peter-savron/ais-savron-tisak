---
name: project-persistence
description: Change this project's local ArcadeDB repositories, document mappers, schema changelogs, and filesystem storage with resource and compatibility checks.
---

# Change local persistence

Read root `AGENTS.md`, the affected repository port, local implementation, mapper,
and schema values. The User feature shows the existing arrangement:

- `core/ports/incoming/UserRepository.java`: domain contract and pagination.
- `integrations/local/impl/UserRepositoryLocalImpl.java`: SQL and directory work.
- `integrations/local/mappers/UserDocumentMapper.java`: document conversion.
- `integrations/local/values/UserArcadeDbValues.java`: document type and changelogs.
- `common/core/integration/local/arcadedb`: shared scripts, provider, and changelog runner.

## Repository and schema changes

Keep SQL and ArcadeDB objects behind repository ports. Bind user-controlled values
as parameters; interpolate only trusted schema identifiers. Reuse shared scripts
when their behavior matches the operation, rather than expanding them speculatively.
Close `ResultSet` and other owned resources with try-with-resources.

Map domain UUIDs and fields consistently with existing documents and Lombok field
constants. Keep conversion in mappers. For a field change, check reads, writes,
schema properties, indexes, and backward compatibility together.

Use the existing changelog mechanism for schema evolution. Add a new version in
the relevant partition instead of rewriting a migration that may already have
executed. Preserve required unique indexes; a pre-insert existence check alone
does not prevent concurrent duplicates.

Make transaction boundaries explicit for writes. Pagination must have defined
ordering, zero-based offsets, and intentional handling of negative values, zero
limit, and offsets beyond the result set. Preserve the current port contract;
the CLI can request all users with `getAll(0, Integer.MAX_VALUE)`.

## Filesystem and key storage

Resolve storage roots through `StorageConfig` and its overrides, not a hard-coded
home directory. Keep resolved paths within their intended storage root. Do not
expose database paths, passwords, or key bytes in logs.

Database and filesystem changes do not share a transaction: identify failure order,
cleanup, and recovery for operations touching both. Avoid deleting pre-existing
data during failed-create cleanup. Use `UserKeyRepository` for key storage and
close temporary `UserKey` handles; do not duplicate storage code in services.

## Verify the boundary

Use the project-tests skill. Test actual query/mapping behavior against an embedded
database in `@TempDir`; exercise relevant pagination edges and schema/index
behavior. Test filesystem cleanup or rollback when those paths change. Use Mockito
for peripheral collaborators, not hand-written repository implementations.
