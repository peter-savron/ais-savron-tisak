---
name: int-adb
description: Use when implementing or troubleshooting ArcadeDB transactions in this project.
---

# ArcadeDB

## Transactions

Wrap record writes in `database.transaction(() -> { ... })`, including deletes and
updates. Keep the transaction open through result consumption
and closure for write commands. Ordinary read queries can run outside a transaction.
