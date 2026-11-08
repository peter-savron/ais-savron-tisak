# User

## Fields

- `name` should be unique
- `context` brief info (20 words) about the user (if user has more roles u could also assign a current-role to the user - this is optional and should be applied in a later phase)
- `config` various user wide configuration
  - `preview-changes` ask for confirmation before running commands

## Actions

### Create User

Creates user and stores his basic details. Sets most values to default. Password hash is stored in a safe place. It is used to authenticate the user, the AES key used for decryption is created at login.

### Update password

Old and new password are required, existing secrets are decrypted with the old key and encrypted again using the new key.

### Login

User key is calculated based on his password and stored on OS keyring. Key is used to decrypt secrets.
Creates a temporary config for the user-session.

### Logout

Key and active user data is removed from the keyring.
Removes all user-session configuration.
If an user is logged out there is no possibility to decode his secrets.

### Update

Updates some or all configurations.

### Delete

If the correct password is given, deletes the user and all his data,
