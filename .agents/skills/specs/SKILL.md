---
name: specs
description: Instructions how to write specification for parts of the app.
---

# Docs

Each doc should be relevant for only a feature, if mentioning another feature you can link the other doc like `(feature)[path/to/feature]`. Separate documentation to those API related in `/api/{interface}` folder (e.g. `api/cli`), those for core functionalities in `/features` and those for integrations in `/integrations/{integrationKind}` (e.g. `/integrations/arcadedb`).

In `/usage` there is info on how to use the app and start it. Docs are written a posteriori and serve the end user. (or another developer that will test the app). Usage is separate by different final apps, as are `/api` specs. Usage should focus on the exposing ui and sequence of action to be taken to reach an end goal and not on the underlying implementation.

## Post-MVP specs

Keep specs for planned features outside the MVP in `docs/todo/` for later implementation. Active specs may link to a todo spec to show that an unimplemented feature already has a spec. Label the link as post-MVP.

When using skills to write code, exclude `docs/todo/` from documentation searches and skip links into it. Read a todo spec for a coding task only when the Master explicitly asks to implement the feature it describes.

## Core

Core layer should handle the bulk of the business logic:

- authorization of the operations that are executed;
- specification of model schemas;
- validation of input and model schemas;
- mutations that are applied to the business objects;
- conditions and mutations that apply to the operation/process and other business logic.

When an operation creates a resource owned by the acting user, specify that core
verifies the user exists before creating it. Operations on existing resources can
use owner-scoped lookups alone when their normal missing-resource behavior applies.

## Storage

storage layer has just two main concerns:

- what is the right kind of storage to be used (when having relational DB, NoSQL, VDB etc. available);
- how to retrieve data from persistent storageM
- how to write data to persistent storage;
- how to specify the schema of the persistent data to be stored, how should data be linked;
- also atomicity and consistency of the write read operations should be considered (this is mostly a storage medium wide concern).
- need for indexes to speed up search.

### Filesystem

One storing files on filesystem it should be specified:

- when to create subfolder;
- how the stored files lay inside the folder structure.

### ArcadeDB

When defining a schema determine whether Document or Node should be used and what should edges look like.

## Api

Api spec specify how should input data be passed by the consumer to the service. Main concerns of API layer are:

- where to retrieve authentication data (when authn data is applicable - it makes sense one applying to online service, for local functionalities will be mostly ignored)
- where to retrieve current context (where is specified who is using the app and where in the app is he located currently);
- how to retrieve input for user in a way that causes the less friction;
- depending on the kind of the API, authentication can be resolved on the API layer. API layer can require the user to be authenticated as part of the input.

For CLI inputs and output, use human-readable selectors and display names. Keep UUIDs in internal references and service calls.

## Updating the skill

You should avoid changing the Masters words as much as possible. You should append new text to the description and if what the Master specified overrides some part of the specs, you should override only that part of the spec, Do not make unnecessary refactors to other parts of the document.


