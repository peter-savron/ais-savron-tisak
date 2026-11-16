package si.fri.ais.user.core.ports.outgoing.impl.local;

import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.commmands.UserLogin;
import si.fri.ais.user.core.models.commmands.UserPasswordUpdate;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;

public class UserAuthnServiceCliImpl implements UserAuthnService {
    @Override
    public void login(UserLogin userLogin) {
        // TODO check password hash and compute key
    }

    @Override
    public void logout(String userName) {
        // TODO delete key via repository and session data (which does not exist yet)
    }

    @Override
    public User updateUserPassword(UserPasswordUpdate passwordUpdate) {
        // TODO Create old key backup for disaster recovery
        // TODO rewrite secrets
        // TODO update password hash on User entity
        // TODO delete disaster recovery key
        return null;
    }
}
