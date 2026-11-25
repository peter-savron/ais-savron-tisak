---
name: storage-local
description: Implement or troubleshoot ArcadeDB and filesystem storage in this project's local integration module.
---

# ArcadeDB

## Transactions

Wrap record writes in `database.transaction(() -> { ... })`, including deletes and
updates. Keep the transaction open through result consumption
and closure for write commands. Ordinary read queries can run outside a transaction.

## Schema initialization

Run each repository's ArcadeDB changelogs from its own `@PostConstruct` method.


## Feature boundaries

Keep repository operations within the schema owned by their feature. If an
operation appears to need to read or modify another feature's schema, ask the
Master whether to proceed before adding that access.

# Local filesystem storage

Read [the storage layout](../../../docs/integrations/local-storage/README.md) and
the feature's filesystem specification before changing a storage path or lifecycle.
Implement filesystem operations in `integration-local-storage` behind the relevant
core repository port.

- Inject `StorageConfig` and resolve paths from its data, cache, or config root
  according to the storage layout. Honor overrides and keep computed paths within
  the intended root, including when a path crosses symbolic links.
- Use a stable application ID for paths that must survive a rename, where the
  feature defines an ID-based layout. Leave existing layouts compatible.
- For operations that also write to ArcadeDB, report success only after both
  stores agree. On failure, clean up only artifacts created by the failed
  operation and restore removed state where the feature contract requires it.
- Preserve component-owned content when a feature requires empty-directory
  deletion. Follow the feature's cleanup order and surface failures so the
  operation can be retried.
