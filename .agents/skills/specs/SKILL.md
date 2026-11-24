---
name: specs
description: Instructions how to write specification for parts of the app.
---

# Docs

Each doc should be relevant for only a feature, if mentioning another feature you can link the other doc like `(feature)[path/to/feature]`. Separate documentation to those API related in `/api/{interface}` folder (e.g. `api/cli`), those for core functionalities in `/core` and those for integrations in `/integrations/{integrationKind}` (e.g. `/integrations/arcadedb`).

In `/usage` there is info on how to use the app and start it. Docs are written a posteriori and serve the end user. (or another developer that will test the app). Usage is separate by different final apps, as are `/api` specs. Usage should focus on the exposing ui and sequence of action to be taken to reach an end goal and not on the underlying implementation.

## Updating the skill

You should avoid changing the Masters words as much as possible. You should append new text to the description and if what the Master specified overrides some part of the specs, you should override only that part of the spec, Do not make unnecessary refactors to other parts of the document.


