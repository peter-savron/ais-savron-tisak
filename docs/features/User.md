# User

## Fields

- `name` should be unique
- `context` brief info (20 words) about the user (if the user has more roles, you could also assign a current role to the user - this is optional and should be applied in a later phase)
- `config` various user-wide configurations
  - `preview-changes` ask for confirmation before running commands

User encryption keys are stored separately, at the moment in the app data folder. More about storage strategies in [Storage](Storage.md#Keys) 

See the [User CLI specification](../api/cli/User.md) for command options and prompts.

## Actions

### Create User

Creates a user and stores his basic details. Sets most values to defaults. Password hash is stored in a safe place. It is used to authenticate the user. The AES key used for decryption is created at login.

Password verification uses Argon2id v1.3 with 64 MiB of memory, 3 iterations,
1 lane, a random 32-byte salt, and a 32-byte hash stored in PHC format.
At login, a separate Argon2id calculation with the same parameters and the user's
persisted `keySalt` derives a 32-byte AES-256 key. Independent salts keep the
stored password verifier separate from the encryption key. The key is passed to
`UserKeyRepository`; the temporary in-memory key is cleared afterward.

The AES derivation parameters must stay stable for existing users. Changing them
requires migrating encrypted secrets. Earlier PBKDF2 password hashes are not
accepted by this implementation; existing accounts require an explicit migration
or recreation.

### Update password

Old and new passwords are required. Existing secrets are decrypted with the old key and encrypted again using the new key.

- Old password is rechecked;
- New password is validated;
- Operations on secrets are temporarily locked;
- Existing secrets dek are decrypted with old and encrypted with new key and updated;
- Password hash and master key are updated.

### Login

User key is calculated based on his password and stored in the OS keyring. Key is used to decrypt secrets.
Creates a temporary config for the user-session.

### Logout

Key and active user data are removed from the keyring.
Removes all user-session configuration.
If a user is logged out, there is no possibility to decode his secrets.

### Update

Updates some or all configurations.

### Delete

If the correct password is given, deletes the user and all his data.
