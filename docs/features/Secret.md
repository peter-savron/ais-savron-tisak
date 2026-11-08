# Secret

Secrets can be tied to an user or a workspace, mostly contain api keys or similar. Secrets stored in the cloud should be handled differently as local secrets.
They should never be passed directly to agents.

## Fields

- `name` name of the secret (unique)
- `type` type of secret (numeric, string, bytes etc.)
- `value` encrypted (authenticated encryption) value of secret (using the key from the keyring)

## Actions

Basic CRUD (with some specifics related to encryption and keys).

### Get secret

Retrieve and decode the secret, user should be prompted for a password again.

### Create secret

Specify target (default is User), could be workspace etc. or similar.

### Update secret

### Remove secret

