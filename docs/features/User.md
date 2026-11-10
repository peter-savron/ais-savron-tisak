# User

## Fields

- `name` should be unique
- `context` brief info (20 words) about the user (if the user has more roles, you could also assign a current role to the user - this is optional and should be applied in a later phase)
- `config` various user-wide configurations
  - `preview-changes` ask for confirmation before running commands

User encryption keys are stored separately, at the moment in the app data folder. More about storage strategies in [Storage](Storage.md#Keys) 

## Actions

### Create User

Creates a user and stores his basic details. Sets most values to defaults. Password hash is stored in a safe place. It is used to authenticate the user. The AES key used for decryption is created at login.

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
