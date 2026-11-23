---
name: specs
description: Instructions how to write specification for parts of the app.
---

# Docs

Each doc should be relevant for only a feature, if mentioning another feature you can link the other doc like `(feature)[path/to/feature]`. Separate documentation to those API related in `/api/{interface}` folder (e.g. `api/cli`), those for core functionalities in `/core` and those for integrations in `/integrations/{integrationKind}` (e.g. `/integrations/arcadedb`).

## Updating the skill

You should avoid changing the Masters words as much as possible. You should append new text to the description and if what the Master specified overrides some part of the specs, you should override only that part of the spec, Do not make unnaccessary refactors to other parts of the document.


